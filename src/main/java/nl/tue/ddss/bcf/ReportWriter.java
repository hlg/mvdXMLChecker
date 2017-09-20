package nl.tue.ddss.bcf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.google.common.collect.Sets;
import org.apache.commons.io.IOUtils;
import org.bimserver.bcf.Bcf;
import org.bimserver.bcf.BcfException;
import org.bimserver.bcf.Issue;
import org.bimserver.bcf.markup.Comment;
import org.bimserver.bcf.markup.CommentStatus;
import org.bimserver.bcf.markup.Header;
import org.bimserver.bcf.markup.Markup;
import org.bimserver.bcf.markup.Topic;
import org.bimserver.bcf.visinfo.Component;
import org.bimserver.bcf.visinfo.Direction;
import org.bimserver.bcf.visinfo.PerspectiveCamera;
import org.bimserver.bcf.visinfo.Point;
import org.bimserver.bcf.visinfo.VisualizationInfo;
import org.bimserver.emf.IfcModelInterface;
import org.bimserver.emf.ModelMetaData;
import org.bimserver.interfaces.objects.SActionState;
import org.bimserver.interfaces.objects.SDeserializerPluginConfiguration;
import org.bimserver.interfaces.objects.SLongActionState;
import org.bimserver.interfaces.objects.SProject;
import org.bimserver.interfaces.objects.SRevision;
import org.bimserver.interfaces.objects.SSerializerPluginConfiguration;
import org.bimserver.models.ifc2x3tc1.IfcProject;
import org.bimserver.models.store.IfcHeader;
import org.bimserver.plugins.deserializers.DeserializeException;
import org.bimserver.plugins.services.BimServerClientInterface;
import org.bimserver.shared.BimServerClientFactory;
import org.bimserver.shared.ChannelConnectionException;
import org.bimserver.shared.exceptions.*;
import org.bimserver.shared.UsernamePasswordAuthenticationInfo;
import org.bimserver.utils.FileDataSource;
import org.xml.sax.SAXException;

public class ReportWriter {
	private String ifcProjectGuid;
	private String ifcHeaderFilename;
	private Date ifcHeaderTimeStamp;
	private Bcf bcf;
	private BimServerClientInterface bimServerClient;
	Set<Long> roids;

	public ReportWriter(IfcModelInterface ifcModel)
			throws DeserializeException {
		bcf = new Bcf();
		List<IfcProject> projects = ifcModel.getAll(IfcProject.class);
		if (projects.size() != 1)
			throw new RuntimeException(
					"The IFC model should have only one IfcProject");
		else {
			for (IfcProject project : projects) {
				ifcProjectGuid = project.getGlobalId();
			}
			ModelMetaData mmd = ifcModel.getModelMetaData();
			IfcHeader ifcHeader = mmd.getIfcHeader();
			ifcHeaderFilename = ifcHeader.getFilename();
			ifcHeaderTimeStamp = ifcHeader.getTimeStamp();
		}
	}
/*
	public void exportToCollada(File ifcFile) throws BimServerClientException {
		try {
			BimServerClientFactory factory = new JsonBimServerClientFactory(null, 
					"http://localhost:8082//");
			bimServerClient = factory
					.create(new UsernamePasswordAuthenticationInfo(
							"c.zhang@tue.nl", "chi"));
			SProject project = bimServerClient.getServiceInterface()
					.addProject("test" + Math.random(), "ifc2x3tc1");
			long poid = project.getOid();
			SDeserializerPluginConfiguration deserializer = bimServerClient
					.getServiceInterface().getDeserializerByName(
							"IfcStepDeserializer");
			bimServerClient.getServiceInterface().checkin(
					project.getOid(), "test", deserializer.getOid(),
					ifcFile.length(), ifcFile.getName(),
					new DataHandler(new FileDataSource(ifcFile)), true,true); // TODO check boolean param
			List<SRevision> revs = bimServerClient.getServiceInterface().getAllRevisionsOfProject(poid);
			long roid = revs.get(revs.size() - 1).getOid();

			SSerializerPluginConfiguration sSerializer;

			sSerializer = bimServerClient.getServiceInterface()
					.getSerializerByName("Collada");
			long downloadId = bimServerClient.getServiceInterface()
					.download(Sets.newHashSet(roid), "", sSerializer.getOid(), true);
			SLongActionState downloadState = bimServerClient.getRegistry()
					.getProgress(downloadId);
			if (downloadState.getState() == SActionState.FINISHED) {
				InputStream inputStream = bimServerClient
						.getServiceInterface()
						.getDownloadData(downloadId).getFile().getInputStream();
				FileOutputStream fileOutputStream = new FileOutputStream(
						File.createTempFile("Model_View_Checker", ""));
				IOUtils.copy(inputStream, fileOutputStream);
				fileOutputStream.close();
			}
		} catch (PublicInterfaceNotFoundException | IOException | ServiceException | ChannelConnectionException e) {
			e.printStackTrace();
		}
	}*/

    private Markup addMarkup(String ifcSpatialStructureElement, String ifcGuid, String comment,String topicGuid){
		Markup markup = new Markup();
		Header header = new Header();
		Header.File headerFile = new Header.File();
		headerFile.setIfcProject(ifcProjectGuid);
		headerFile.setIfcSpatialStructureElement(ifcSpatialStructureElement);
		headerFile.setFilename(ifcHeaderFilename);
		GregorianCalendar gregorianCalender = new GregorianCalendar();
		gregorianCalender.setTime(ifcHeaderTimeStamp);
		try {
			headerFile.setDate(DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(gregorianCalender));
		} catch (DatatypeConfigurationException e1) {
			e1.printStackTrace();
		}
		header.getFile().add(headerFile);
		markup.setHeader(header);

		Topic topic = new Topic();
		topic.setGuid(topicGuid);
		topic.setReferenceLink("None Available"); // e.g. URL to external mvdXML
													// file
		topic.setTitle("Issue regarding: " + ifcGuid);
		markup.setTopic(topic);

		Comment comments = new Comment();
		try {
			comments.setDate(DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(new GregorianCalendar()));
		} catch (DatatypeConfigurationException e1) {
			e1.printStackTrace();
		}
		String commentGuid = UUID.randomUUID().toString();
		comments.setGuid(commentGuid);
		comments.setVerbalStatus("Open");
		comments.setStatus(CommentStatus.ERROR);
		String commentAuthor = System.getProperty("user.name");
		comments.setAuthor(commentAuthor);
		comments.setComment(comment);
		Comment.Topic commentTopic = new Comment.Topic();
		commentTopic.setGuid(topicGuid);
		comments.setTopic(commentTopic);
		markup.getComment().add(comments);
		
		return markup;
    }
    
    private VisualizationInfo addVisInfo(String ifcGuid,List<String> ifcGuids) throws SAXException, ParserConfigurationException{
    	TempGeometry tempGeometry = new TempGeometry();
//		tempGeometry.cleanUp(ifcGuids);
		VisualizationInfo visualizationInfo = new VisualizationInfo();

		Component component1 = new Component();
		component1.setIfcGuid(ifcGuid);
		component1.setOriginatingSystem("BCFReportWriter");
		component1.setAuthoringToolId("BCFReportWriter");

		VisualizationInfo.Components components = new VisualizationInfo.Components();
		visualizationInfo.setComponents(components);
		components.getComponent().add(component1);

		Direction cameraDirection = new Direction();
		cameraDirection.setX(tempGeometry.cameraDirectionX);
		cameraDirection.setY(tempGeometry.cameraDirectionY);
		cameraDirection.setZ(tempGeometry.cameraDirectionZ);

		Direction cameraUpVector = new Direction();
		cameraUpVector.setX(tempGeometry.cameraUpVectorX);
		cameraUpVector.setY(tempGeometry.cameraUpVectorY);
		cameraUpVector.setZ(tempGeometry.cameraUpVectorZ);

		Point cameraViewPoint = new Point();
		cameraViewPoint.setX(tempGeometry.cameraViewPointX);
		cameraViewPoint.setY(tempGeometry.cameraViewPointY);
		cameraViewPoint.setZ(tempGeometry.cameraViewPointZ);

		PerspectiveCamera perspectiveCamera = new PerspectiveCamera();
		perspectiveCamera.setFieldOfView(45.0);
		perspectiveCamera.setCameraUpVector(cameraUpVector);
		perspectiveCamera.setCameraViewPoint(cameraViewPoint);
		perspectiveCamera.setCameraDirection(cameraDirection);

		visualizationInfo.setPerspectiveCamera(perspectiveCamera);
		visualizationInfo.setLines(new VisualizationInfo.Lines());
		visualizationInfo
				.setClippingPlanes(new VisualizationInfo.ClippingPlanes());
		
		return visualizationInfo;
    }
	
    
    
    public void addIssue(String ifcSpatialStructureElement, String ifcGuid,
			String comment, List<String> ifcGuids) throws SAXException,
			ParserConfigurationException {
    	UUID uuid=UUID.randomUUID();
		Markup markup=addMarkup(ifcSpatialStructureElement, ifcGuid, comment,uuid.toString());
		VisualizationInfo visInfo=addVisInfo(ifcGuid,ifcGuids);		
		Issue issue = new Issue(uuid,markup,visInfo);
		bcf.addIssue(issue);
	}

	public void writeReport(OutputStream outputStream) {
		try {
			bcf.write(outputStream);
		} catch (BcfException | IOException e) {
			e.printStackTrace();
		}
	}
}