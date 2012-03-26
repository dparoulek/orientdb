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
package com.orientechnologies.orient.core.db;

import java.util.List;
import java.util.Set;

import com.orientechnologies.orient.core.command.OCommandRequest;
import com.orientechnologies.orient.core.db.record.ODatabaseRecord;
import com.orientechnologies.orient.core.dictionary.ODictionary;
import com.orientechnologies.orient.core.hook.ORecordHook;
import com.orientechnologies.orient.core.hook.ORecordHook.TYPE;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.iterator.ORecordIteratorCluster;
import com.orientechnologies.orient.core.metadata.OMetadata;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.query.OQuery;
import com.orientechnologies.orient.core.record.ORecordInternal;
import com.orientechnologies.orient.core.tx.OTransaction;
import com.orientechnologies.orient.core.tx.OTransaction.TXTYPE;

@SuppressWarnings("unchecked")
public abstract class ODatabaseRecordWrapperAbstract<DB extends ODatabaseRecord> extends ODatabaseWrapperAbstract<DB> implements
		ODatabaseComplex<ORecordInternal<?>> {

	public ODatabaseRecordWrapperAbstract(final DB iDatabase) {
		super(iDatabase);
		iDatabase.setDatabaseOwner(this);
	}

	public OTransaction getTransaction() {
		return underlying.getTransaction();
	}

	public ODatabaseComplex<ORecordInternal<?>> begin() {
		return underlying.begin();
	}

	public ODatabaseComplex<ORecordInternal<?>> begin(final TXTYPE iType) {
		return underlying.begin(iType);
	}

	public ODatabaseComplex<ORecordInternal<?>> begin(final OTransaction iTx) {
		return underlying.begin(iTx);
	}

	public ODatabaseComplex<ORecordInternal<?>> commit() {
		return underlying.commit();
	}

	public ODatabaseComplex<ORecordInternal<?>> rollback() {
		return underlying.rollback();
	}

	public OUser getUser() {
		return underlying.getUser();
	}

	public OMetadata getMetadata() {
		return underlying.getMetadata();
	}

	public ODictionary<ORecordInternal<?>> getDictionary() {
		return underlying.getDictionary();
	}

	public Class<? extends ORecordInternal<?>> getRecordType() {
		return underlying.getRecordType();
	}

	public <REC extends ORecordInternal<?>> ORecordIteratorCluster<REC> browseCluster(final String iClusterName) {
		return underlying.browseCluster(iClusterName);
	}

	public <REC extends ORecordInternal<?>> ORecordIteratorCluster<REC> browseCluster(final String iClusterName,
			final Class<REC> iRecordClass) {
		return underlying.browseCluster(iClusterName, iRecordClass);
	}

	public <RET extends OCommandRequest> RET command(final OCommandRequest iCommand) {
		return (RET) underlying.command(iCommand);
	}

	public <RET extends List<?>> RET query(final OQuery<? extends Object> iCommand, final Object... iArgs) {
		return (RET) underlying.query(iCommand, iArgs);
	}

	public <RET extends Object> RET newInstance() {
		return (RET) underlying.newInstance();
	}

	public ODatabaseComplex<ORecordInternal<?>> delete(final ORecordInternal<?> iRecord) {
		underlying.delete(iRecord);
		return this;
	}

	public <RET extends ORecordInternal<?>> RET load(final ORID iRecordId) {
		return (RET) underlying.load(iRecordId);
	}

	public <RET extends ORecordInternal<?>> RET load(final ORID iRecordId, final String iFetchPlan) {
		return (RET) underlying.load(iRecordId, iFetchPlan);
	}

	public <RET extends ORecordInternal<?>> RET load(final ORecordInternal<?> iRecord) {
		return (RET) underlying.load(iRecord);
	}

	public <RET extends ORecordInternal<?>> RET load(final ORecordInternal<?> iRecord, final String iFetchPlan) {
		return (RET) underlying.load(iRecord, iFetchPlan);
	}

	public void reload(final ORecordInternal<?> iRecord) {
		underlying.reload(iRecord);
	}

	public void reload(final ORecordInternal<?> iRecord, final String iFetchPlan) {
		underlying.reload(iRecord, iFetchPlan);
	}

	public ODatabaseComplex<ORecordInternal<?>> save(final ORecordInternal<?> iRecord, final String iClusterName) {
		underlying.save(iRecord, iClusterName);
		return this;
	}

	public ODatabaseComplex<ORecordInternal<?>> save(final ORecordInternal<?> iRecord) {
		underlying.save(iRecord);
		return this;
	}

	public boolean isRetainRecords() {
		return underlying.isRetainRecords();
	}

	public ODatabaseRecord setRetainRecords(boolean iValue) {
		underlying.setRetainRecords(iValue);
		return (ODatabaseRecord) this;
	}

	public ORecordInternal<?> getRecordByUserObject(final Object iUserObject, final boolean iMandatory) {
		if (databaseOwner != this)
			return getDatabaseOwner().getRecordByUserObject(iUserObject, false);

		return (ORecordInternal<?>) iUserObject;
	}

	public void registerPojo(final Object iObject, final ORecordInternal<?> iRecord) {
		if (databaseOwner != this)
			getDatabaseOwner().registerPojo(iObject, iRecord);
	}

	public Object getUserObjectByRecord(final ORecordInternal<?> iRecord, final String iFetchPlan) {
		if (databaseOwner != this)
			return databaseOwner.getUserObjectByRecord(iRecord, iFetchPlan);

		return iRecord;
	}

	public boolean existsUserObjectByRID(final ORID iRID) {
		if (databaseOwner != this)
			return databaseOwner.existsUserObjectByRID(iRID);
		return false;
	}

	public <DBTYPE extends ODatabaseRecord> DBTYPE checkSecurity(final String iResource, final int iOperation) {
		return (DBTYPE) underlying.checkSecurity(iResource, iOperation);
	}

	public <DBTYPE extends ODatabaseRecord> DBTYPE checkSecurity(final String iResourceGeneric, final int iOperation,
			final Object... iResourcesSpecific) {
		return (DBTYPE) underlying.checkSecurity(iResourceGeneric, iOperation, iResourcesSpecific);
	}

	public <DBTYPE extends ODatabaseComplex<?>> DBTYPE registerHook(final ORecordHook iHookImpl) {
		underlying.registerHook(iHookImpl);
		return (DBTYPE) this;
	}

	public boolean callbackHooks(final TYPE iType, final Object iObject) {
		return underlying.callbackHooks(iType, iObject);
	}

	public Set<ORecordHook> getHooks() {
		return underlying.getHooks();
	}

	public <DBTYPE extends ODatabaseComplex<?>> DBTYPE unregisterHook(final ORecordHook iHookImpl) {
		underlying.unregisterHook(iHookImpl);
		return (DBTYPE) this;
	}
}
