package org.jamopp.refactoring.publicize.action;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.jamopp.refactoring.publicize.PublicizeRefactoring;

public class CallPublicizeRefactoringAction implements IObjectActionDelegate {

	private StructuredSelection selection = null;
	

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	public void run(IAction action) {
		if (selection == null) {
			return;
		}
		final ResourceSet resourceSet = new ResourceSetImpl();
		final Map<Resource, URI> javaSourceFiles2BundlesMap = new LinkedHashMap<Resource, URI>();
		for (Iterator<?> i = selection.iterator(); i.hasNext(); ) {
			Object next = i.next();
			if (next instanceof IResource) {
				IResource workspaceResource  = (IResource) next;
				try {
					workspaceResource.accept(new IResourceVisitor() {
						@Override
						public boolean visit(IResource resource) throws CoreException {
							if (resource instanceof IFile && resource.getName().endsWith(".java")) {
								URI javaSourceURI = URI.createPlatformResourceURI(
										resource.getFullPath().toString(), true);
								URI bundleURI = URI.createPlatformResourceURI(
										resource.getProject().getFullPath().toString(), true);
								Resource javaSourceFileResource = 
										resourceSet.getResource(javaSourceURI, true);
								javaSourceFiles2BundlesMap.put(javaSourceFileResource, bundleURI);
							}
							return true;
						}
					});
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		}
		
		new PublicizeRefactoring().publicizeFriends(javaSourceFiles2BundlesMap);
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof StructuredSelection) {
			this.selection = (StructuredSelection) selection;
		} else {
			this.selection = null;
		}
	}

}
