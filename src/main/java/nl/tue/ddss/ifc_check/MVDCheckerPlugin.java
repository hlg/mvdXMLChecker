package nl.tue.ddss.ifc_check;

import org.bimserver.models.store.ObjectDefinition;
import org.bimserver.plugins.PluginConfiguration;
import org.bimserver.plugins.PluginContext;
import org.bimserver.plugins.PluginManagerInterface;
import org.bimserver.shared.exceptions.PluginException;
import org.bimserver.plugins.modelchecker.ModelChecker;
import org.bimserver.plugins.modelchecker.ModelCheckerPlugin;

public class MVDCheckerPlugin implements ModelCheckerPlugin{

	@Override
	public void init(PluginContext pluginContext) throws PluginException {
		// TODO Auto-generated method stub
	}

	@Override
	public ObjectDefinition getSettingsDefinition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ModelChecker createModelChecker(PluginConfiguration arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
}