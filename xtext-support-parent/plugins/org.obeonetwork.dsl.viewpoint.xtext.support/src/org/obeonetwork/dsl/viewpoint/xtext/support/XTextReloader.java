package org.obeonetwork.dsl.viewpoint.xtext.support;

import java.io.IOException;
import java.util.Map;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.CompareUI;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.util.BasicMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.domain.ICompareEditingDomain;
import org.eclipse.emf.compare.domain.impl.EMFCompareEditingDomain;
import org.eclipse.emf.compare.ide.ui.internal.configuration.EMFCompareConfiguration;
import org.eclipse.emf.compare.ide.ui.internal.editor.ComparisonEditorInput;
import org.eclipse.emf.compare.merge.BatchMerger;
import org.eclipse.emf.compare.merge.IBatchMerger;
import org.eclipse.emf.compare.merge.IMerger;
import org.eclipse.emf.compare.rcp.EMFCompareRCPPlugin;
import org.eclipse.emf.compare.scope.DefaultComparisonScope;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.workspace.AbstractEMFOperation;
import org.eclipse.sirius.business.internal.session.danalysis.DAnalysisSessionImpl;
import org.eclipse.sirius.business.internal.session.danalysis.Reloader;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;

import com.google.common.collect.Maps;
import com.google.inject.Injector;

public class XTextReloader extends Reloader {

    private Injector xtextInjector;

    public XTextReloader(Injector injector) {
        this.xtextInjector = injector;
    }

    @Override
    public IStatus reload(Resource resource, DAnalysisSessionImpl session) throws IOException {
        if (resource instanceof XtextResource) {
            return mergeExternalChanges((XtextResource) resource, session);
        } else {
            return super.reload(resource, session);
        }
    }

    private IStatus mergeExternalChanges(final XtextResource resourceInSirius, DAnalysisSessionImpl session) throws IOException {
        try {
            final XtextResource virtualResource = createVirtualXtextResource(resourceInSirius.getURI(), resourceInSirius.getResourceSet().getLoadOptions());

            IComparisonScope scope = new DefaultComparisonScope(resourceInSirius, virtualResource, null);
            final Comparison comparison = EMFCompare.builder().build().compare(scope);

            IMerger.Registry mergerRegistry = EMFCompareRCPPlugin.getDefault().getMergerRegistry();
            final IBatchMerger merger = new BatchMerger(mergerRegistry);

            final TransactionalEditingDomain ted = session.getTransactionalEditingDomain();
            AbstractEMFOperation operation = new AbstractEMFOperation(ted, "Update resource after external change", Maps.newHashMap()) {

                protected IStatus doExecute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
                    merger.copyAllRightToLeft(comparison.getDifferences(), new BasicMonitor());
                    return org.eclipse.core.runtime.Status.OK_STATUS;
                }
            };
            return operation.execute(new NullProgressMonitor(), null);
        } catch (Exception e) {
            Activator.logError(e);
            return super.reload(resourceInSirius, session);
        }
    }

    private XtextResource createVirtualXtextResource(URI uri, Map<Object, Object> loadOptions) throws IOException {
        XtextResourceSet rs = xtextInjector.getInstance(XtextResourceSet.class);
        rs.setClasspathURIContext(getClass());
        rs.getLoadOptions().putAll(loadOptions);

        // Create virtual resource
        XtextResource xtextVirtualResource = (XtextResource) rs.getResource(uri, true);
        // XtextResource xtextVirtualResource = (XtextResource)
        // resourceFactory.createResource(URI.createURI(uri.toString()));
        // xtextVirtualResource.load(loadOptions);
        // rs.getResources().add(xtextVirtualResource);

        EcoreUtil.resolveAll(xtextVirtualResource);
        return xtextVirtualResource;
    }

    private void openCompareDialog(Resource resourceInSirius, Resource virtualResource, Comparison comparison) {
        ICompareEditingDomain editingDomain = EMFCompareEditingDomain.create(resourceInSirius, virtualResource, null);
        AdapterFactory adapterFactory = new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
        CompareConfiguration compareConfiguration = new CompareConfiguration();
        compareConfiguration.setRightEditable(false);
        compareConfiguration.setRightLabel("External Change");
        CompareEditorInput input = new ComparisonEditorInput(new EMFCompareConfiguration(compareConfiguration), comparison, editingDomain, adapterFactory);
        CompareUI.openCompareDialog(input);
    }
}
