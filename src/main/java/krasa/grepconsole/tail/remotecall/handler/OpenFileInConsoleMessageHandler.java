package krasa.grepconsole.tail.remotecall.handler;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.ex.WindowManagerEx;
import krasa.grepconsole.action.TailFileInConsoleAction;
import krasa.grepconsole.plugin.TailHistory;
import krasa.grepconsole.utils.FocusUtils;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author Vojtech Krasa
 */

public class OpenFileInConsoleMessageHandler implements MessageHandler {
	private static final Logger log = Logger.getInstance(OpenFileInConsoleMessageHandler.class);
	protected String lastProject;
	protected Date lastProjectSet;

	@Override
	public void handleMessage(final String message) {
		if (message != null && !message.isEmpty()) {
			log.info("Opening file=" + message);
			final Date date = new Date();

			ApplicationManager.getApplication().invokeLater(new Runnable() {
				@Override
				public void run() {
					IdeFrame[] allProjectFrames = WindowManager.getInstance().getAllProjectFrames();
					List<String> values = getValues(allProjectFrames);
					String selectedProject = null;

					if (values.size() > 1) {
						String initialProject = getInitiallySelectedProject(values, date);

						int i = Messages.showChooseDialog("Select Project Frame", "Select Project Frame",
								values.toArray(new String[values.size()]), initialProject, Messages.getQuestionIcon());
						if (i >= 0) {
							selectedProject = values.get(i);
						}

					} else if (values.size() == 1) {
						selectedProject = values.get(0);
					} else {
						log.warn("Cannot open file, no projects opened");
						return;
					}
					if (selectedProject != null) {
						lastProject = selectedProject;
						lastProjectSet = new Date();
						Project project = getProject(allProjectFrames, selectedProject);
						if (project != null) {
							final File file = new File(message);
							TailHistory.getState(project).add(file);

							TailFileInConsoleAction.openFileInConsole(project, file, TailFileInConsoleAction.resolveEncoding(file));
						}
					}
				}

				public String getInitiallySelectedProject(List<String> values, Date date) {
					WindowManagerEx instance = (WindowManagerEx) WindowManager.getInstance();
					Window focusedWindow = instance.getMostRecentFocusedWindow();
					String initialProject = null;

					// multiple files opened at once
					if (lastProjectSet != null && date.getTime() < lastProjectSet.getTime()) {
						if (values.contains(lastProject)) {
							initialProject = lastProject;
						}
					}

					// current frame
					if (initialProject == null && focusedWindow instanceof IdeFrame) {
						Project project = ((IdeFrame) focusedWindow).getProject();
						if (project != null) {
							initialProject = project.getName();
							FocusUtils.requestFocus(project);
						}
					}

					// floating window
					if (initialProject == null) {
						initialProject = lastProject;
					}

					// nothing worked or project closed
					if (initialProject == null || !values.contains(initialProject)) {
						initialProject = values.get(0);
					}
					return initialProject;
				}

				private List<String> getValues(IdeFrame[] allProjectFrames) {
					List<String> values = new ArrayList<>();
					for (int i = 0; i < allProjectFrames.length; i++) {
						IdeFrame allProjectFrame = allProjectFrames[i];
						final Project project = allProjectFrame.getProject();
						if (project != null) {
							values.add(project.getName());
						}
					}
					Collections.sort(values);
					return values;
				}

				private Project getProject(IdeFrame[] allProjectFrames, String projectName) {
					Project project1 = null;
					for (int i = 0; i < allProjectFrames.length; i++) {
						IdeFrame allProjectFrame = allProjectFrames[i];
						final Project project = allProjectFrame.getProject();
						if (project != null && projectName.equals(project.getName())) {
							project1 = project;
							break;
						}
					}
					return project1;
				}
			});
		}
	}
}
