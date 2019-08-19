/*-
 * ===============LICENSE_START=======================================================
 * Acumos
 * ===================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property & Tech Mahindra. All rights reserved.
 * ===================================================================================
 * This Acumos software file is distributed by AT&T and Tech Mahindra
 * under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * This file is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ===============LICENSE_END=========================================================
 */
package org.acumos.elk.client.transport;

import io.swagger.annotations.ApiModelProperty;

public class ArchiveInfo {

	@ApiModelProperty(required = true, value = "snapshot creation date")
	String date;

	@ApiModelProperty(required = true, value = "snapshot name.")
	String backUpName;

	public ArchiveInfo(String date, String backUpName) {
		super();
		this.date = date;
		this.backUpName = backUpName;
	}

	public ArchiveInfo() {
		// TODO Auto-generated constructor stub
	}

	public String getDate() {
		return date;
	}

	public String getBackUpName() {
		return backUpName;
	}

	@Override
	public String toString() {
		return "ArchiveInfo [date=" + date + ", backUpName=" + backUpName + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((backUpName == null) ? 0 : backUpName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ArchiveInfo other = (ArchiveInfo) obj;
		if (backUpName == null) {
			if (other.backUpName != null)
				return false;
		} else if (!backUpName.equals(other.backUpName))
			return false;
		return true;
	}

}
