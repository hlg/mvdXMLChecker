package org.bimserver.plugin.modelviewchecker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBException;

import nl.tue.ddss.ifc_check.IfcChecker;
import nl.tue.ddss.ifc_check.MVDConstraint;
import nl.tue.ddss.ifc_check.MvdXMLParser;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.interfaces.objects.SExtendedData;
import org.bimserver.interfaces.objects.SExtendedDataSchema;
import org.bimserver.interfaces.objects.SFile;
import org.bimserver.interfaces.objects.SObjectType;
import org.bimserver.interfaces.objects.SProject;
import org.bimserver.interfaces.objects.SSerializerPluginConfiguration;
import org.bimserver.models.ifc2x3tc1.IfcProject;
import org.bimserver.shared.exceptions.BimServerClientException;
import org.bimserver.plugins.services.BimServerClientInterface;
import org.bimserver.plugins.services.NewRevisionHandler;
import org.bimserver.shared.exceptions.PublicInterfaceNotFoundException;
import org.bimserver.shared.exceptions.ServerException;
import org.bimserver.shared.exceptions.UserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelViewCheckerNewRevisionHandler implements NewRevisionHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(ModelViewCheckerNewRevisionHandler.class);
	private MvdXMLParser mvdXMLParser;
	private String targetNameSpace;

	public ModelViewCheckerNewRevisionHandler(byte[] mvdXMLData, ClassLoader classLoader, String targetNameSpace) {
		this.targetNameSpace = targetNameSpace;
		try {
			mvdXMLParser = new MvdXMLParser(new ByteArrayInputStream(mvdXMLData), classLoader);
		} catch (JAXBException e) {
			LOGGER.error("", e);
		}
	}

	@SuppressWarnings("unused")
	public void newRevision(BimServerClientInterface bimServerClientInterface, long poid, long roid, String userToken, long soid, SObjectType settings)
			throws ServerException, UserException {
		SSerializerPluginConfiguration sSerializer;
		try {
			SProject project = bimServerClientInterface.getServiceInterface().getProjectByPoid(poid);
			IfcModelInterface model = bimServerClientInterface.getModel(project, roid, false, false);
			ByteArrayOutputStream bcfOutput = new ByteArrayOutputStream();
			try {
				List<MVDConstraint> constraints = mvdXMLParser.generateConceptTrees();
				for (MVDConstraint constraint : constraints) {
					IfcChecker ifcChecker = new IfcChecker(model, constraint);
					ifcChecker.checkIfcModel(bcfOutput);
				}
			} catch (JAXBException e) {
				e.printStackTrace();
			}

			List<IfcProject> ifcProjects = model.getAll(IfcProject.class);
			IfcProject mainIfcProject = null;
			if (!ifcProjects.isEmpty()) {
				mainIfcProject = ifcProjects.get(0);
			}

			SExtendedDataSchema extendedDataSchemaByNamespace = bimServerClientInterface.getServiceInterface().getExtendedDataSchemaByName( targetNameSpace);

			SFile file = new SFile();

			SExtendedData extendedData = new SExtendedData();
			extendedData.setTitle("ModelViewChecker Results");
			file.setFilename("modelviewchecker.bcfzip");
			extendedData.setSchemaId(extendedDataSchemaByNamespace.getOid());
			try {
				byte[] bytes = bcfOutput.toByteArray();
				file.setData(bytes);
				file.setMime("application/bcf");

				long fileId = bimServerClientInterface.getServiceInterface().uploadFile(file);
				extendedData.setFileId(fileId);

				bimServerClientInterface.getServiceInterface().addExtendedDataToRevision(roid, extendedData);
			} catch (Exception e) {
				LOGGER.error("", e);
			}
		} catch (PublicInterfaceNotFoundException e) {
			LOGGER.error("", e);
		} catch (BimServerClientException e) {
			LOGGER.error("", e);
		}
	}
}
