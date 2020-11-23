package krasa.grepconsole.integration;

import com.intellij.execution.actions.*;
import com.intellij.execution.impl.*;
import com.intellij.execution.ui.*;
import com.intellij.icons.*;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.util.*;
import krasa.grepconsole.action.*;
import krasa.grepconsole.grep.*;
import krasa.grepconsole.plugin.*;
import krasa.grepconsole.stats.*;
import krasa.grepconsole.stats.action.*;
import org.jetbrains.annotations.*;

import java.util.*;

public class MyConsoleActionsPostProcessor extends ConsoleActionsPostProcessor {

	@NotNull
	@Override
	public AnAction[] postProcess(@NotNull ConsoleView console, @NotNull AnAction[] actions) {
		ServiceManager serviceManager = ServiceManager.getInstance();
		serviceManager.registerConsole(console);
//		serviceManager.createHighlightFilterIfMissing(console);

		if (console instanceof ConsoleViewImpl) {
			StatisticsManager.createStatisticsPanels((com.intellij.execution.impl.ConsoleViewImpl) console);
		}

		ArrayList<AnAction> anActions = new ArrayList<>();
		anActions.add(new OpenConsoleSettingsAction(console));
		anActions.add(new PreviousHighlightAction(console));
		anActions.add(new NextHighlightAction(console));
		if (console instanceof ConsoleViewImpl) {
			try {
				//API check}
				if (MoveErrorStreamToTheBottomAction.getConsoleViewContentTypeKey() != null) {
					anActions.add(new MoveErrorStreamToTheBottomAction((ConsoleViewImpl) console));
				}
			} catch (Throwable e) {
				//ok
			}
		}
		anActions.addAll(Arrays.asList(actions));
		replaceClearAction(anActions);
		return anActions.toArray(new AnAction[anActions.size()]);
	}

	/**
	 * not cached
	 */
	@NotNull
	@Override
	public AnAction[] postProcessPopupActions(@NotNull ConsoleView console, @NotNull AnAction[] actions) {
		ServiceManager manager = ServiceManager.getInstance();
		ArrayList<AnAction> anActions = new ArrayList<>();
		anActions.add(new OpenGrepConsoleAction("Grep", "Open a new filter/grep console", AllIcons.General.Filter));
		anActions.add(new AddHighlightAction("Add highlight", "Add highlight for this selected text", IconLoader.findIcon("/krasa/grepconsole/icons/highlight.png")));
		if (manager.isRegistered(console)) {
			anActions.add(new ShowHideStatisticsConsolePanelAction(console));
			anActions.add(new ShowHideStatisticsStatusBarPanelAction(console));
		}
		anActions.add(new OpenConsoleSettingsAction(console));
		anActions.add(new Separator());
		anActions.addAll(Arrays.asList(super.postProcessPopupActions(console, actions)));
		replaceClearAction(anActions);
		return anActions.toArray(new AnAction[anActions.size()]);
	}

	private void replaceClearAction(ArrayList<AnAction> anActions) {
		for (int i = 0; i < anActions.size(); i++) {
			AnAction anAction = anActions.get(i);
			if (anAction instanceof ClearConsoleAction) {
				anActions.set(i, clearAction());
			}
		}
	}

	private ClearConsoleAction clearAction() {
		return new ClearConsoleAction() {
			@Override
			public void actionPerformed(AnActionEvent e) {
				super.actionPerformed(e);
				final ConsoleView consoleView = e.getData(LangDataKeys.CONSOLE_VIEW);
				if (consoleView != null) {
					try {
						StatisticsManager.clearCount(consoleView);
					} catch (Exception e1) {
						// tough luck
					}
				}
			}
		};
	}

}
