/*
 * Copyright 2016 Sai Pullabhotla.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jmethods.catatumbo.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import com.google.cloud.datastore.BaseEntity;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.NullValue;
import com.google.cloud.datastore.ProjectionEntity;
import com.google.cloud.datastore.Value;
import com.jmethods.catatumbo.DefaultDatastoreKey;
import com.jmethods.catatumbo.EntityManagerException;

/**
 * Converts Entities retrieved from the Cloud Datastore into Entity POJOs.
 *
 * @author Sai Pullabhotla
 */
public class Unmarshaller {

	/**
	 * Input - Native Entity to unmarshal, could be a ProjectionEntity or an
	 * Entity
	 */
	private BaseEntity<?> nativeEntity;

	/**
	 * Output - unmarshalled object
	 */
	private Object entity;

	/**
	 * Entity metadata
	 */
	private EntityMetadata entityMetadata;

	/**
	 * Creates a new instance of <code>Unmarshaller</code>.
	 * 
	 * @param nativeEntity
	 *            the native entity to unmarshal
	 * @param entityClass
	 *            the expected model type
	 */
	private Unmarshaller(BaseEntity<?> nativeEntity, Class<?> entityClass) {
		this.nativeEntity = nativeEntity;
		entityMetadata = EntityIntrospector.introspect(entityClass);

	}

	/**
	 * Unmarshals the given native Entity into an object of given type,
	 * entityClass.
	 * 
	 * @param <T>
	 *            target object type
	 * @param nativeEntity
	 *            the native Entity
	 * @param entityClass
	 *            the target type
	 * @return Object that is equivalent to the given native entity. If the
	 *         given <code>datastoreEntity</code> is <code>null</code>, returns
	 *         <code>null</code>.
	 */
	public static <T> T unmarshal(Entity nativeEntity, Class<T> entityClass) {
		return unmarshalBaseEntity(nativeEntity, entityClass);
	}

	/**
	 * Unmarshals the given native ProjectionEntity into an object of given
	 * type, entityClass.
	 * 
	 * @param <T>
	 *            target object type
	 * @param nativeEntity
	 *            the native Entity
	 * @param entityClass
	 *            the target type
	 * @return Object that is equivalent to the given native entity. If the
	 *         given <code>datastoreEntity</code> is <code>null</code>, returns
	 *         <code>null</code>.
	 */
	public static <T> T unmarshal(ProjectionEntity nativeEntity, Class<T> entityClass) {
		return unmarshalBaseEntity(nativeEntity, entityClass);
	}

	/**
	 * Unmarshals the given BaseEntity and returns the equivalent model object.
	 * 
	 * @param nativeEntity
	 *            the native entity to unmarshal
	 * @param entityClass
	 *            the target type of the model class
	 * @return the model object
	 */
	private static <T> T unmarshalBaseEntity(BaseEntity<?> nativeEntity, Class<T> entityClass) {
		if (nativeEntity == null) {
			return null;
		}
		Unmarshaller unmarshaller = new Unmarshaller(nativeEntity, entityClass);
		return unmarshaller.unmarshal();
	}

	/**
	 * Unmarshals the given Datastore Entity and returns the equivalent Entity
	 * POJO.
	 *
	 * @param <T>
	 *            type
	 * @return the entity POJO
	 */
	private <T> T unmarshal() {

		try {
			instantiateEntity();
			unmarshalIdentifier();
			unmarshalKeyAndParentKey();
			unmarshalProperties();
			return (T) entity;
		} catch (Exception ex) {
			throw new EntityManagerException(ex.getMessage(), ex);
		}
	}

	private void instantiateEntity() throws NoSuchMethodException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		Class<?> entityClass = entityMetadata.getEntityClass();
		Constructor<?> constructor = entityClass.getConstructor();
		entity = constructor.newInstance();
	}

	private void unmarshalIdentifier()
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		IdentifierMetadata identifierMetadata = entityMetadata.getIdentifierMetadata();
		Method writeMethod = identifierMetadata.getWriteMethod();
		writeMethod.invoke(entity, ((Key) nativeEntity.key()).nameOrId());
	}

	private void unmarshalKeyAndParentKey()
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		KeyMetadata keyMetadata = entityMetadata.getKeyMetadata();
		if (keyMetadata != null) {
			Method writeMethod = keyMetadata.getWriteMethod();
			Key entityKey = (Key) nativeEntity.key();
			writeMethod.invoke(entity, new DefaultDatastoreKey(entityKey));
		}

		ParentKeyMetadata parentKeyMetadata = entityMetadata.getParentKeyMetadata();
		if (parentKeyMetadata != null) {
			Method writeMethod = parentKeyMetadata.getWriteMethod();
			Key parentKey = nativeEntity.key().parent();
			if (parentKey != null) {
				writeMethod.invoke(entity, new DefaultDatastoreKey(parentKey));
			}
		}
	}

	private void unmarshalProperties()
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Set<String> properties = nativeEntity.names();
		for (String property : properties) {
			unmarshalProperty(property);
		}
	}

	private void unmarshalProperty(String property)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		PropertyMetadata propertyMetadata = entityMetadata.getPropertyMetadata(property);
		if (propertyMetadata == null) {
			// System.out.println("Unmodeled property: " + property);
			// TODO not in the model. Perhaps add support for unmodeled
			// properties. This could also mean that the property is modeled,
			// but has @Ignore annotation.
			return;
		}
		Value<?> datastoreValue = nativeEntity.getValue(property);
		Object entityValue = null;
		if (!(datastoreValue instanceof NullValue)) {
			PropertyConverter converter = propertyMetadata.getDataType().getConverter();
			entityValue = converter.toObject(datastoreValue);
			Method writeMethod = propertyMetadata.getWriteMethod();
			writeMethod.invoke(entity, entityValue);
		}
	}

}
