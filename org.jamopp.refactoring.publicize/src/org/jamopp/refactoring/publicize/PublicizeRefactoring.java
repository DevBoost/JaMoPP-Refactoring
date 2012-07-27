package org.jamopp.refactoring.publicize;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.emftext.language.java.commons.Commentable;
import org.emftext.language.java.members.Method;
import org.emftext.language.java.references.MethodCall;

public class PublicizeRefactoring {
	
	public void publicizeFriends(Map<Resource, URI> javaSourceFiles2BundlesMap) {
		Set<Resource> modifiedResources = new LinkedHashSet<Resource>();
		for (Resource javaSourceFileResource : javaSourceFiles2BundlesMap.keySet()) {
			for (TreeIterator<EObject> i = javaSourceFileResource.getAllContents(); i
					.hasNext();) {
				EObject next = i.next();
				if (next instanceof MethodCall) {
					MethodCall methodCall = (MethodCall) next;
					Method method = (Method) methodCall.getTarget();
					if (method.isPublic()) {
						continue;
					}
					if (method.isPrivate()) {
						continue;
					}
					if (inSameCompilationUint(method, methodCall)) {
						continue;
					}
					if (!inSamePackage(method, methodCall)) {
						continue;
					}
					if (inSameBundle(method, methodCall, javaSourceFiles2BundlesMap)) {
						continue;
					}
					cleanLayout(method);
					
					method.makePublic();
					
					modifiedResources.add(method.eResource());
				}
			}
		}
		
		for (Resource resource : modifiedResources) {
			try {
				resource.save(null);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private boolean inSameCompilationUint(Commentable e1, Commentable e2) {
		return e1.getContainingCompilationUnit().equals(
				e2.getContainingCompilationUnit());
	}

	private boolean inSamePackage(Commentable e1, Commentable e2) {
		return e1.getContainingCompilationUnit().getNamespaces().equals(
				e2.getContainingCompilationUnit().getNamespaces());
	}

	private boolean inSameBundle(Commentable e1, Commentable e2, 
			Map<Resource, URI> javaSourceFiles2BundlesMap) {
		
		return javaSourceFiles2BundlesMap.get(e1.eResource()).equals(
				javaSourceFiles2BundlesMap.get(e2.eResource()));
	}

	private void cleanLayout(Method method) {
		Commentable elementWithLayout = null;
		if (method.getAnnotationsAndModifiers().isEmpty()) {
			elementWithLayout = method.getTypeReference();
		} else {
			elementWithLayout = method.getAnnotationsAndModifiers().get(0);
		}
		elementWithLayout.getLayoutInformations().clear();
	    
		method.makePublic();
	}

}
