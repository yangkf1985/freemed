/*
 * $Id$
 *
 * Authors:
 *      Jeff Buchbinder <jeff@freemedsoftware.org>
 *
 * FreeMED Electronic Medical Record and Practice Management System
 * Copyright (C) 1999-2012 FreeMED Software Foundation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package org.freemedsoftware.gwt.client.Module;

import java.util.HashMap;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface RemittBillingTransportAsync {
	public void GetRebillList(AsyncCallback<HashMap<String, String>[]> callback);

	public void GetReport(String report, String reportType, AsyncCallback<String> callback);

	public void GetStatus(String[] uniqueIds, AsyncCallback<HashMap<String, String>> callback);

	public void MarkAsBilled(Integer[] ids, AsyncCallback<Boolean> callback);

	public void PatientsToBill(AsyncCallback<HashMap<String, String>[]> callback);

	public void GetClaimInformation(Integer claimIds, AsyncCallback<HashMap<String, String>[]> callback);

	public void ProcessClaims(Integer[] patientIds,
			Integer[] claimIds, HashMap<String, String>[] overrideInfo, AsyncCallback<HashMap<String, String>[]> callback);
}
