package org.eclipse.sirius.example.fowlerdsl.design;

import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.business.api.session.SessionManagerListener;
import org.eclipse.sirius.business.internal.session.danalysis.DAnalysisSessionImpl;
import org.eclipse.xtext.example.fowlerdsl.ui.internal.StatemachineActivator;
import org.obeonetwork.dsl.viewpoint.xtext.support.XTextReloader;

import com.google.inject.Injector;

public class FowlerSessionManagerListener extends SessionManagerListener.Stub {

    @Override
    public void notifyAddSession(Session session) {
        if (session instanceof DAnalysisSessionImpl) {
            Injector injector = StatemachineActivator.getInstance().getInjector("org.eclipse.xtext.example.fowlerdsl.Statemachine");
            ((DAnalysisSessionImpl) session).setReloader(new XTextReloader(injector));
        }
    }
}
