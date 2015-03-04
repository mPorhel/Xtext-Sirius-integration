/*******************************************************************************
 * Copyright (c) 2015 Obeo.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.sirius.example.fowlerdsl.design;

import java.util.Collection;

import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.common.core.service.IOperation;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.CreateDecoratorsOperation;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecoratorTarget;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecoratorTarget.Direction;
import org.eclipse.sirius.diagram.DDiagram;
import org.eclipse.sirius.diagram.DDiagramElement;
import org.eclipse.sirius.diagram.description.DiagramDescription;
import org.eclipse.sirius.diagram.ui.edit.api.part.IDiagramElementEditPart;
import org.obeonetwork.dsl.viewpoint.xtext.support.XtextSemanticValidationDecoratorProvider;

import com.google.common.collect.Lists;

/**
 * @was-generated
 */
public class FowlerValidationDecoratorProvider extends XtextSemanticValidationDecoratorProvider {

    private static final String STATE_MACHINE_DIAGRAM_DESCRIPTION_ID = "State Machine Diagram";

    private static final String FOWLER_FAST_VALIDATION = "org.eclipse.xtext.example.fowlerdsl.ui.statemachine.check.fast";

    private static final String FOWLER_NORMAL_VALIDATION = "org.eclipse.xtext.example.fowlerdsl.ui.statemachine.check.normal";

    private static final String FOWLER_EXPENSIVE_VALIDATION = "org.eclipse.xtext.example.fowlerdsl.ui.statemachine.check.expensive";

    private static final Collection<String> MARKER_TYPES = Lists.newArrayList(FOWLER_FAST_VALIDATION, FOWLER_NORMAL_VALIDATION, FOWLER_EXPENSIVE_VALIDATION);

    private static final String KEY = "FowlerSemanticValidationStatus"; //$NON-NLS-1$

    protected String getKey() {
        return KEY;
    }

    protected Collection<String> getMarkerTypes() {
        return MARKER_TYPES;
    }

    @Override
    protected Direction getDirection() {
        return IDecoratorTarget.Direction.SOUTH_EAST;
    }

    @Override
    public boolean provides(IOperation operation) {
        boolean provide = super.provides(operation);
        if (provide) {
            IDecoratorTarget decoratorTarget = ((CreateDecoratorsOperation) operation).getDecoratorTarget();
            EditPart editPart = (EditPart) decoratorTarget.getAdapter(EditPart.class);
            if (editPart instanceof IDiagramElementEditPart) {
                DDiagramElement dde = ((IDiagramElementEditPart) editPart).resolveDiagramElement();
                DDiagram parentDiagram = dde == null ? null : dde.getParentDiagram();
                DiagramDescription ddesc = parentDiagram == null ? null : parentDiagram.getDescription();

                // Provide and compute the fowler validation decorator only for
                // state machine diagrams.
                provide = ddesc != null && STATE_MACHINE_DIAGRAM_DESCRIPTION_ID.equals(ddesc.getName());
            }
        }
        return provide;
    }

}
