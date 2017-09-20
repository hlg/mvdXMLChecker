package nl.tue.ddss.ifc_check;

import org.bimserver.interfaces.objects.SPluginType;
import org.bimserver.models.store.ObjectDefinition;
import org.bimserver.plugins.Plugin;
import org.bimserver.plugins.PluginContext;
import org.bimserver.plugins.PluginManagerInterface;
import org.bimserver.shared.exceptions.PluginException;

public class MvdChecker implements Plugin{

	@Override
	public ObjectDefinition getSettingsDefinition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SPluginType getPluginType() {
		return null;
	}

	@Override
	public void init(PluginContext arg0) throws PluginException {
		// TODO Auto-generated method stub
		
	}

}