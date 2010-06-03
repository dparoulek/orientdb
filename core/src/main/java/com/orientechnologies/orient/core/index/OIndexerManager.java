/*
 * Copyright 1999-2010 Luca Garulli (l.garulli--at--orientechnologies.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.orientechnologies.orient.core.index;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.orientechnologies.common.log.OLogManager;
import com.orientechnologies.orient.core.hook.ORecordHookAbstract;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.record.ORecord;
import com.orientechnologies.orient.core.record.ORecordSchemaAware;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.type.tree.OTreeMapDatabase;

/**
 * Handles indexing when records change.
 * 
 * @author Luca Garulli
 * 
 */
public class OIndexerManager extends ORecordHookAbstract {

	@Override
	public void onRecordAfterCreate(final ORecord<?> iRecord) {
		final Map<OProperty, String> indexedProperties = getIndexedProperties(iRecord);

		if (indexedProperties != null)
			for (Entry<OProperty, String> propEntry : indexedProperties.entrySet()) {
				propEntry.getKey().getIndex().put(propEntry.getValue(), (ODocument) iRecord);
			}
	}

	@Override
	public void onRecordAfterUpdate(final ORecord<?> iRecord) {
		final Map<OProperty, String> indexedProperties = getIndexedProperties(iRecord);

		if (indexedProperties != null) {
			ODocument doc = (ODocument) iRecord;
			final Set<String> dirtyFields = doc.getDirtyFields();

			if (dirtyFields != null && dirtyFields.size() > 0) {
				// REMOVE INDEX OF ENTRIES FOR THE OLD VALUES
				for (Entry<OProperty, String> propEntry : indexedProperties.entrySet()) {
					if (dirtyFields.contains(propEntry.getKey().getName()))
						// REMOVE IT
						propEntry.getKey().getIndex().remove(propEntry.getValue());
				}

				// ADD INDEX OF ENTRIES FOR THE CHANGED ONLY VALUES
				for (Entry<OProperty, String> propEntry : indexedProperties.entrySet()) {
					if (dirtyFields.contains(propEntry.getKey().getName()))
						propEntry.getKey().getIndex().put(propEntry.getValue(), (ODocument) iRecord);
				}
			}
		}
	}

	@Override
	public void onRecordAfterDelete(final ORecord<?> iRecord) {
		final Map<OProperty, String> indexedProperties = getIndexedProperties(iRecord);

		if (indexedProperties != null) {
			ODocument doc = (ODocument) iRecord;
			final Set<String> dirtyFields = doc.getDirtyFields();

			if (dirtyFields != null && dirtyFields.size() > 0) {
				// REMOVE INDEX OF ENTRIES FOR THE OLD VALUES
				for (Entry<OProperty, String> propEntry : indexedProperties.entrySet()) {
					if (dirtyFields.contains(propEntry.getKey().getName()))
						// REMOVE IT
						propEntry.getKey().getIndex().remove(propEntry.getValue());
				}
			}

			// REMOVE INDEX OF ENTRIES FOR THE CHANGED ONLY VALUES
			for (Entry<OProperty, String> propEntry : indexedProperties.entrySet()) {
				if (doc.containsField(propEntry.getKey().getName()) && !dirtyFields.contains(propEntry.getKey().getName()))
					propEntry.getKey().getIndex().remove(propEntry.getValue());
			}
		}
	}

	protected Map<OProperty, String> getIndexedProperties(final ORecord<?> iRecord) {
		if (!(iRecord instanceof ORecordSchemaAware<?>))
			return null;

		final ORecordSchemaAware<?> record = (ORecordSchemaAware<?>) iRecord;
		final OClass cls = record.getSchemaClass();
		if (cls == null)
			return null;

		OTreeMapDatabase<String, ODocument> index;
		Object fieldValue;
		String fieldValueString;

		Map<OProperty, String> indexedProperties = null;
		ORecord<?> indexedRecord;

		for (OProperty prop : cls.properties()) {
			index = prop.getIndex();
			if (index != null) {
				fieldValue = record.field(prop.getName());

				if (fieldValue != null) {
					fieldValueString = fieldValue.toString();

					indexedRecord = index.get(fieldValueString);
					if (indexedRecord != null && !indexedRecord.equals(iRecord))
						OLogManager.instance().exception("Found duplicated key '%s' for property '%s'", null, OIndexException.class,
								fieldValueString, prop);

					// PUSh THE PROPERTY IN THE SET TO BE WORKED BY THE EXTERNAL
					if (indexedProperties == null)
						indexedProperties = new HashMap<OProperty, String>();
					indexedProperties.put(prop, fieldValueString);
				}
			}
		}

		return indexedProperties;
	}
}
