/*******************************************************************************
 * Copyright (c) 2011 Subgraph.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Subgraph - initial API and implementation
 ******************************************************************************/
package com.subgraph.vega.ui.http;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.subgraph.vega.api.http.proxy.IHttpInterceptor;
import com.subgraph.vega.api.http.proxy.IHttpInterceptorEventHandler;
import com.subgraph.vega.api.http.proxy.IHttpProxyService;
import com.subgraph.vega.api.http.proxy.IHttpProxyServiceEventHandler;
import com.subgraph.vega.api.http.proxy.IProxyTransaction;
import com.subgraph.vega.internal.ui.http.ProxyStatusLineContribution;

public class ProxyServiceTrackerCustomizer implements ServiceTrackerCustomizer {
	private final BundleContext context;
	private final ProxyStatusLineContribution statusLineContribution;
	private IHttpProxyServiceEventHandler proxyEventHandler;
	private IHttpInterceptor interceptor;
	private IHttpInterceptorEventHandler interceptorEventHandler;

	ProxyServiceTrackerCustomizer(BundleContext context, ProxyStatusLineContribution statusLineContribution) {
		this.context = context;
		this.statusLineContribution = statusLineContribution;
		proxyEventHandler = new IHttpProxyServiceEventHandler() {
			@Override
			public void notifyStart(int listenerCount) {
				handleNotifyStart(listenerCount);
			}

			@Override
			public void notifyStop() {
				handleNotifyStop();
			}

			@Override
			public void notifyConfigChange(int listenerCount) {
				handleNotifyStart(listenerCount);
			}
		};
		interceptorEventHandler = new IHttpInterceptorEventHandler() {
			@Override
			public void notifyQueue(IProxyTransaction transaction, int idx) {
				handleNotifyQueue(transaction);
			}

			@Override
			public void notifyRemove(int idx) {
				handleNotifyRemove();
			}

			@Override
			public void notifyEmpty() {
				handleNotifyEmpty();
			}
		};
	}
	
	@Override
	public Object addingService(ServiceReference reference) {
		IHttpProxyService proxyService = (IHttpProxyService) context.getService(reference);
		proxyService.registerEventHandler(proxyEventHandler);
		if (proxyService.isRunning()) {
			statusLineContribution.setProxyRunning(proxyService.getListenerConfigsCount());
		} else {
			statusLineContribution.setProxyStopped();
		}
		interceptor = proxyService.getInterceptor();
		interceptor.addEventHandler(interceptorEventHandler);
		return proxyService;
	}

	@Override
	public void modifiedService(ServiceReference reference, Object service) {
		// TODO Auto-generated method stub
	}

	@Override
	public void removedService(ServiceReference reference, Object service) {
		// TODO Auto-generated method stub

	}

	private void handleNotifyStart(int numListeners) {
		statusLineContribution.setProxyRunning(numListeners);
	}

	private void handleNotifyStop() {
		statusLineContribution.setProxyStopped();
	}
	
	private void handleNotifyQueue(IProxyTransaction transaction) {
		statusLineContribution.setProxyPending(interceptor.transactionQueueSize());
	}
	
	private void handleNotifyRemove() {
		statusLineContribution.setProxyPending(interceptor.transactionQueueSize());
	}

	private void handleNotifyEmpty() {
		statusLineContribution.setProxyPending(0);
	}
	
}
