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
package org.acumos.elk.client.service;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.acumos.elk.client.transport.ArchiveInfo;
import org.acumos.elk.client.transport.ELkRepositoryMetaData;
import org.acumos.elk.client.transport.ElkArchiveRequest;
import org.acumos.elk.client.transport.ElkArchiveResponse;
import org.acumos.elk.client.transport.ElkGetRepositoriesResponse;
import org.acumos.elk.client.transport.ElkRepositoriesRequest;
import org.acumos.elk.client.transport.ErrorTransport;
import org.acumos.elk.client.utils.ElkClientConstants;
import org.acumos.elk.client.utils.ElkServiceUtils;
import org.elasticsearch.action.admin.cluster.repositories.delete.DeleteRepositoryRequest;
import org.elasticsearch.action.admin.cluster.repositories.get.GetRepositoriesRequest;
import org.elasticsearch.action.admin.cluster.repositories.get.GetRepositoriesResponse;
import org.elasticsearch.action.admin.cluster.repositories.put.PutRepositoryRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.cluster.metadata.RepositoryMetaData;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.repositories.fs.FsRepository;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


/**
 * Implementation of operation related to elastic stack repository.
 *
 */
@Service
public class SnapshotRepositoryServiceImpl extends AbstractELKClientConnection implements ISnapshotRepositoryService {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	@Override
	public ElkGetRepositoriesResponse getAllElkRepository() {

		logger.debug("Inside getAllElkRepository ");
		RestHighLevelClient client = restHighLevelClientConnection();
		GetRepositoriesRequest request1 = new GetRepositoriesRequest();
		request1.repositories();
		request1.local(true);

		try {
			GetRepositoriesResponse response = client.snapshot().getRepository(request1, RequestOptions.DEFAULT);
			List<RepositoryMetaData> repositoryMetaDataResponse = response.repositories();
			logger.debug("Number of repositoryMetaDataResponse size {}", repositoryMetaDataResponse.size());

			ElkGetRepositoriesResponse elkRepositoriesResponse = new ElkGetRepositoriesResponse();
			List<ELkRepositoryMetaData> repositories = new ArrayList<>();
			for (RepositoryMetaData snapshotStatusResponse : repositoryMetaDataResponse) {
				logger.debug("\nNAME: {} \n TYPE: {} \n SETTINGS: {}", snapshotStatusResponse.name(),
						snapshotStatusResponse.type(), snapshotStatusResponse.settings());
				ELkRepositoryMetaData repositoryMetaData = new ELkRepositoryMetaData();
				repositoryMetaData.setName(snapshotStatusResponse.name());
				repositoryMetaData.setType(snapshotStatusResponse.type());
				JSONObject settings = new JSONObject();
				for (String key : snapshotStatusResponse.settings().keySet()) {
					settings.put(key, snapshotStatusResponse.settings().get(key));
				}
				repositoryMetaData.setSettings(settings);
				repositories.add(repositoryMetaData);
			}
			elkRepositoriesResponse.setRepositories(repositories);
			client.close();
			return elkRepositoriesResponse;
		} catch (Exception ex) {
			logger.debug("Exception:", ex);
			throw new ErrorTransport("Unable to connect Elasticserach");
		}

	}

	public String createElkRepository(ElkRepositoriesRequest elkCreateRepositoriesRequest) {
		logger.debug("Inside createElkRepository ");
		if (StringUtils.isEmpty(elkCreateRepositoriesRequest.getRepositoryName())) {
			return "false | RepositoryName empty is not allowed";
		}
		ElkGetRepositoriesResponse response = getAllElkRepository();
		List<ELkRepositoryMetaData> elkRepositoryMetaDataList = response.getRepositories();
		Set<String> repositoryNameSet = new HashSet<>();
		for (ELkRepositoryMetaData eLkRepositoryMetaData : elkRepositoryMetaDataList) {
			repositoryNameSet.add(eLkRepositoryMetaData.getName());
		}
		if (repositoryNameSet.contains(elkCreateRepositoriesRequest.getRepositoryName())) {
			return "false | RepositoryName already exist";
		}
		RestHighLevelClient client = restHighLevelClientConnection();
		PutRepositoryRequest request = new PutRepositoryRequest();
		String locationKey = FsRepository.LOCATION_SETTING.getKey();
		String locationValue = elkCreateRepositoriesRequest.getRepositoryName().trim();
		String compressKey = FsRepository.COMPRESS_SETTING.getKey();
		boolean compressValue = true;
		Settings settings = Settings.builder().put(locationKey, locationValue).put(compressKey, compressValue).build();
		request.settings(settings);
		request.name(elkCreateRepositoriesRequest.getRepositoryName().trim());
		request.type(FsRepository.TYPE);
		if (StringUtils.isEmpty(elkCreateRepositoriesRequest.getNodeTimeout())) {
			request.masterNodeTimeout(ElkClientConstants.TIME_ONE_MINT_OUT);
		} else {
			request.masterNodeTimeout(elkCreateRepositoriesRequest.getNodeTimeout());
		}
		request.verify(true);
		AcknowledgedResponse acknowledgedResponse;
		boolean acknowledged = false;
		try {
			acknowledgedResponse = client.snapshot().createRepository(request, RequestOptions.DEFAULT);
			acknowledged = acknowledgedResponse.isAcknowledged();
			client.close();
		} catch (IOException ex) {
			logger.debug("Exception:", ex);
			throw new ErrorTransport("Unable to connect Elasticserach");
		}

		logger.debug("Repository is created(true for created)  {}", acknowledged);
		return String.valueOf(acknowledged);
	}

	@Override
	public String deleteElkRepository(ElkRepositoriesRequest elkDeleteRepositoriesRequest) {

		logger.debug("Inside deleteElkRepository");
		RestHighLevelClient client = restHighLevelClientConnection();
		DeleteRepositoryRequest deleteRepositoryRequest = new DeleteRepositoryRequest(
				elkDeleteRepositoriesRequest.getRepositoryName());
		deleteRepositoryRequest.masterNodeTimeout(elkDeleteRepositoriesRequest.getNodeTimeout());
		
		AcknowledgedResponse deleteAcknowledgedResponse;
		try {
			deleteAcknowledgedResponse = client.snapshot().deleteRepository(deleteRepositoryRequest,
					RequestOptions.DEFAULT);
			client.close();
		} catch (IOException ex) {
			logger.debug("Exception:", ex);
			throw new ErrorTransport("Unable to connect Elasticserach");
		}
		boolean deleteAcknowledged = deleteAcknowledgedResponse.isAcknowledged();
		logger.debug("Repository is delete(true for created)... {}", deleteAcknowledged);

		return String.valueOf(deleteAcknowledged);
	}

	@Override
	public ElkArchiveResponse getArchiveElkRepository() throws Exception {
		String action = ElkClientConstants.INFO;
		ElkArchiveResponse elkArchiveResponse = archiveOperation(null, action);
		return elkArchiveResponse;
	}

	@Override
	public ElkArchiveResponse archiveElkRepository(ElkArchiveRequest archiveRequest) throws Exception {
		logger.debug("Inside archiveElkRepository");
		String action = archiveRequest.getAction();
		ElkArchiveResponse elkArchiveResponse = archiveOperation(archiveRequest, action);
		return elkArchiveResponse;
	}

	private ElkArchiveResponse archiveOperation(ElkArchiveRequest archiveRequest, String action) throws Exception {
		logger.debug("Inside archiveOperation action:{}", action);
		Function<String, ArchiveInfo> f = str -> {
			String[] p = str.split(",");
			ArchiveInfo archiveInfo1 = new ArchiveInfo();
			if (p[0] != null && p[0].length() > 0 && p[1] != null && p[1].length() > 0) {
				archiveInfo1 = new ArchiveInfo(p[0], p[1]);
			}
			return archiveInfo1;
		};

		String result = null;
		String[] archiveInfoArray;
		List<String> resultList = new ArrayList<>();
		if (action.equalsIgnoreCase(ElkClientConstants.INFO)) {
			try {
				result = ElkServiceUtils.executeScript(action, "NA");
				resultList.add(result.trim());
			} catch (Exception ex) {
				logger.debug("Exception:", ex);
				throw new Exception("Error occured elk archive operation");
			}
		} else {
			try {
				for (String repoName : archiveRequest.getRepositoryName()) {
					result = ElkServiceUtils.executeScript(action, repoName);
					resultList.add(result.trim());
				}
			} catch (Exception ex) {
				logger.debug("Exception:", ex);
				throw new Exception("Error occured elk archive operation");
			}
		}
		boolean chkCSV = result.contains(",");
		logger.debug("chkCSV:{}", chkCSV);
		ElkArchiveResponse elkArchiveResponse = new ElkArchiveResponse();
		List<ArchiveInfo> archiveInfoList = new ArrayList<ArchiveInfo>();
		if (chkCSV) {
			for (String resultOuput : resultList) {
				archiveInfoArray = resultOuput.split("\n");
				for (String archiveInfo : archiveInfoArray) {
					archiveInfoList.add(f.apply(archiveInfo));
				}
			}
			elkArchiveResponse.setMsg("Action:" + action + " done");
			elkArchiveResponse.setStatus(ElkClientConstants.SUCCESS);
			elkArchiveResponse.setArchiveInfo(archiveInfoList);
			logger.debug("archiveInfoList:" + archiveInfoList);
			if (action.equalsIgnoreCase("RESTORE")) {
				for (ArchiveInfo archiveInfo : archiveInfoList) {
					ElkRepositoriesRequest elkCreateRepositoriesRequest = new ElkRepositoriesRequest();
					elkCreateRepositoriesRequest.setRepositoryName(archiveInfo.getBackUpName());
					elkCreateRepositoriesRequest.setNodeTimeout(ElkClientConstants.TIME_ONE_MINT_OUT);
					createElkRepository(elkCreateRepositoriesRequest);
				}
			}

		} else {
			result = result.replace("\n", "");
			elkArchiveResponse.setStatus(ElkClientConstants.FAIL);
			elkArchiveResponse.setMsg(result);
		}
		return elkArchiveResponse;
	}

}
