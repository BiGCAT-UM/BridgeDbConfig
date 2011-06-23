package org.pathvisio.bridgedb;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.pathvisio.desktop.plugin.Plugin;

public class Activator implements BundleActivator
{

	@Override
	public void start(BundleContext context) throws Exception {

		BridgeDbConfigPlugin plugin = new BridgeDbConfigPlugin();
		context.registerService(Plugin.class.getName(), plugin, null);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// TODO Auto-generated method stub
	}
}
