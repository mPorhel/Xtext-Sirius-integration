/*******************************************************************************
 * Copyright (c) 2007, 2014 THALES GLOBAL SERVICES and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Obeo - initial API and implementation
 *******************************************************************************/
package org.obeonetwork.dsl.viewpoint.xtext.support;

import java.util.Collection;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.common.core.service.AbstractProvider;
import org.eclipse.gmf.runtime.common.core.service.IOperation;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.AbstractDecorator;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.CreateDecoratorsOperation;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecoration;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecorator;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecoratorProvider;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecoratorTarget;
import org.eclipse.gmf.runtime.draw2d.ui.mapmode.MapModeUtil;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.sirius.diagram.DiagramPlugin;
import org.eclipse.sirius.diagram.ui.edit.api.part.IAbstractDiagramNodeEditPart;
import org.eclipse.sirius.diagram.ui.edit.api.part.IDiagramEdgeEditPart;
import org.eclipse.sirius.diagram.ui.edit.api.part.IDiagramElementEditPart;
import org.eclipse.sirius.diagram.ui.internal.edit.parts.DNodeListElementEditPart;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.xtext.resource.XtextResource;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * @was-generated
 */
public abstract class XtextSemanticValidationDecoratorProvider extends AbstractProvider implements IDecoratorProvider {

    private ListMultimap<String, IDecorator> decorators = ArrayListMultimap.create();

    protected abstract String getKey();

    protected abstract Collection<String> getMarkerTypes();

    protected abstract IDecoratorTarget.Direction getDirection();

    @Override
    public boolean provides(IOperation operation) {
        boolean provide = false;
        if (operation instanceof CreateDecoratorsOperation) {
            IDecoratorTarget decoratorTarget = ((CreateDecoratorsOperation) operation).getDecoratorTarget();
            EditPart editPart = (EditPart) decoratorTarget.getAdapter(EditPart.class);
            if (editPart instanceof IAbstractDiagramNodeEditPart || editPart instanceof IDiagramEdgeEditPart || editPart instanceof DNodeListElementEditPart) {
                IDiagramElementEditPart part = (IDiagramElementEditPart) editPart;
                EObject semanticElement = part.resolveTargetSemanticElement();
                provide = semanticElement != null && semanticElement.eResource() instanceof XtextResource;
            }
        }
        return provide;
    }

    /**
     * @was-generated
     */
    @Override
    public void createDecorators(IDecoratorTarget decoratorTarget) {
        final EditPart editPart = (EditPart) decoratorTarget.getAdapter(EditPart.class);
        if (editPart instanceof IDiagramElementEditPart) {
            decoratorTarget.installDecorator(getKey(), new StatusDecorator(decoratorTarget));
        }
    }

    // TODO Look for editing domain issue in SiriusValidationProvider
    // private void refreshDecorators(String viewId, Diagram diagram) {
    // final List<IDecorator> decoratorsToRefresh = viewId != null ?
    // decorators.get(viewId) : null;
    // if (decoratorsToRefresh == null || decoratorsToRefresh.isEmpty() ||
    // diagram == null) {
    // return;
    // }
    //
    // final Diagram fdiagram = diagram;
    // PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
    //
    // @Override
    // public void run() {
    // try {
    // TransactionUtil.getEditingDomain(fdiagram).runExclusive(new Runnable() {
    //
    // @Override
    // public void run() {
    // for (IDecorator decorator : decoratorsToRefresh) {
    // decorator.refresh();
    // }
    // }
    // });
    // } catch (Exception e) {
    //                    DiagramPlugin.getDefault().logError("Decorator refresh failure", e); //$NON-NLS-1$
    // }
    // }
    // });
    // }

    private class StatusDecorator extends AbstractDecorator {

        public StatusDecorator(IDecoratorTarget decoratorTarget) {
            super(decoratorTarget);
        }

        @Override
        public void refresh() {
            removeDecoration();
            View view = (View) getDecoratorTarget().getAdapter(View.class);
            if (view == null || view.eResource() == null) {
            }

            EditPart editPart = (EditPart) getDecoratorTarget().getAdapter(EditPart.class);
            if (editPart == null || editPart.getParent() == null || editPart.getViewer() == null) {
                return;
            }

            EObject resolveSemanticElement = null;
            Resource semanticResource = null;
            if (editPart instanceof IDiagramElementEditPart) {
                IDiagramElementEditPart part = (IDiagramElementEditPart) editPart;
                resolveSemanticElement = part.resolveTargetSemanticElement();
                semanticResource = resolveSemanticElement.eResource();
            }
            if (semanticResource == null) {
                return;
            }

            int severity = IMarker.SEVERITY_INFO;
            IMarker foundMarker = null;

            final Collection<IMarker> markers = Sets.newLinkedHashSet();
            findMarkers(semanticResource, markers);

            if (markers.isEmpty()) {
                return;
            }

            Label toolTip = null;
            URI uri = EcoreUtil.getURI(resolveSemanticElement);
            for (IMarker marker : markers) {
                String attribute = marker.getAttribute("URI_KEY", ""); //$NON-NLS-1$
                if (attribute.equals(uri.toString())) {
                    int nextSeverity = marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
                    Image nextImage = getImage(nextSeverity);
                    if (foundMarker == null) {
                        foundMarker = marker;
                        toolTip = new Label(marker.getAttribute(IMarker.MESSAGE, ""), //$NON-NLS-1$
                                nextImage);
                    } else {
                        if (toolTip.getChildren().isEmpty()) {
                            Label comositeLabel = new Label();
                            FlowLayout fl = new FlowLayout(false);
                            fl.setMinorSpacing(0);
                            comositeLabel.setLayoutManager(fl);
                            comositeLabel.add(toolTip);
                            toolTip = comositeLabel;
                        }
                        toolTip.add(new Label(marker.getAttribute(IMarker.MESSAGE, ""), //$NON-NLS-1$
                                nextImage));
                    }
                    severity = (nextSeverity > severity) ? nextSeverity : severity;
                }
            }
            if (foundMarker != null && editPart instanceof org.eclipse.gef.GraphicalEditPart) {
                computeDecoration(view, (org.eclipse.gef.GraphicalEditPart) editPart, severity, toolTip);
            }
        }

        private void computeDecoration(View view, org.eclipse.gef.GraphicalEditPart editPart, int severity, Label toolTip) {
            IDecoration decoration = null;

            if (view instanceof Diagram) {
                // There is not yet defined decorator for a diagram
            } else if (view instanceof Edge) {
                decoration = getDecoratorTarget().addConnectionDecoration(getImage(severity), 50, true);
            } else {
                int margin = -1;
                margin = MapModeUtil.getMapMode(editPart.getFigure()).DPtoLP(margin);
                decoration = getDecoratorTarget().addShapeDecoration(getImage(severity), getDirection(), margin, true);
            }

            if (decoration != null) {
                setDecoration(decoration);

                // getDecaration() returns a {@link Decoration} instead of a
                // {@link IDecoration}
                getDecoration().setToolTip(toolTip);
            }
        }

        private void findMarkers(Resource semanticResource, Collection<IMarker> accumulator) {
            IResource resource = WorkspaceSynchronizer.getFile(semanticResource);
            if (resource == null || !resource.exists()) {
                return;
            }

            for (String markerType : getMarkerTypes()) {
                IMarker[] markers = null;
                try {
                    markers = resource.findMarkers(markerType, true, IResource.DEPTH_INFINITE);
                } catch (CoreException e) {
                    DiagramPlugin.getDefault().logError("Semantic validation markers (" + markerType + ") refresh failure", e); //$NON-NLS-1$
                }

                if (markers != null && markers.length > 0) {
                    accumulator.addAll(Lists.newArrayList(markers));
                }
            }
        }

        private Image getImage(int severity) {
            String imageName = ISharedImages.IMG_OBJS_ERROR_TSK;
            switch (severity) {
            case IMarker.SEVERITY_ERROR:
                imageName = ISharedImages.IMG_OBJS_ERROR_TSK;
                break;
            case IMarker.SEVERITY_WARNING:
                imageName = ISharedImages.IMG_OBJS_WARN_TSK;
                break;
            default:
                imageName = ISharedImages.IMG_OBJS_INFO_TSK;
            }
            return PlatformUI.getWorkbench().getSharedImages().getImage(imageName);
        }

        @Override
        public void activate() {
            // See in SiriusValidationProvider.StatusDecorator to return if
            // fragmentURI is null

            // add self to global decorators registry
            // TODO replace viewId by fragment uri in decorators.put(viewId,
            // this);

            // start listening to changes in resources
            // TODO add a FileObserver to the FileChangeManager to react to
            // IMarker changes.
            // See
            // org.eclipse.sirius.diagram.ui.internal.providers.SiriusValidationDecoratorProvider.MarkerObserver
            // but use the framgentURI instead of the viewID.
        }

        @Override
        public void deactivate() {
            // See in SiriusValidationProvider.StatusDecorator to return if
            // fragmentURI is null

            // remove self from global decorators registry
            // TODO replace viewId by fragmentURI decorators.remove(viewId,
            // this);

            // stop listening to changes in resources if there are no more
            // decorators
            // TODO Remove the file oberserver
            // if (fileObserver != null && decorators.isEmpty()) {
            // FileChangeManager.getInstance().removeFileObserver(fileObserver);
            // fileObserver = null;
            // }
            super.deactivate();
        }
    }
}
