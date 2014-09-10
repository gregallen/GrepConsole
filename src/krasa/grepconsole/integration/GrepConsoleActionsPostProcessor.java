package krasa.grepconsole.integration;

import java.util.ArrayList;
import java.util.Arrays;

import krasa.grepconsole.action.AddHighlightAction;
import krasa.grepconsole.action.OpenConsoleSettingsAction;
import krasa.grepconsole.filter.AnsiInputFilter;
import krasa.grepconsole.filter.GrepHighlightFilter;
import krasa.grepconsole.plugin.ServiceManager;
import krasa.grepconsole.stats.StatisticsManager;
import krasa.grepconsole.stats.action.ShowHideStatisticsConsolePanelAction;
import krasa.grepconsole.stats.action.ShowHideStatisticsStatusBarPanelAction;

import org.jetbrains.annotations.NotNull;

import com.intellij.execution.actions.ConsoleActionsPostProcessor;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;

public class GrepConsoleActionsPostProcessor extends ConsoleActionsPostProcessor {

	@NotNull
	@Override
	public AnAction[] postProcess(@NotNull ConsoleView console, @NotNull AnAction[] actions) {
		registerConsole(console);
		if (console instanceof ConsoleViewImpl) {
			StatisticsManager.createStatisticsPanels((com.intellij.execution.impl.ConsoleViewImpl) console);
		}

		ArrayList<AnAction> anActions = new ArrayList<AnAction>();
		anActions.add(new OpenConsoleSettingsAction(console));
		anActions.addAll(Arrays.asList(actions));

		replaceClearAction(anActions);
		return anActions.toArray(new AnAction[anActions.size()]);
	}

	private void registerConsole(ConsoleView console) {
		ServiceManager manager = ServiceManager.getInstance();
		GrepHighlightFilter lastGrepHighlightFilter = manager.getLastGrepHighlightFilter();
		if (lastGrepHighlightFilter != null) {
			manager.register(console, lastGrepHighlightFilter);
		}
		AnsiInputFilter lastAnsi = manager.getLastAnsi();
		if (lastAnsi != null && !lastAnsi.isRegistered()) {
			lastAnsi.setConsole(console);
		}
	}

	@NotNull
	@Override
	public AnAction[] postProcessPopupActions(@NotNull ConsoleView console, @NotNull AnAction[] actions) {
		ServiceManager manager = ServiceManager.getInstance();
		ArrayList<AnAction> anActions = new ArrayList<AnAction>();
		anActions.add(new AddHighlightAction("Add highlight", "Add highlight for this selected text", null));
		if (manager.isRegistered(console)) {
			anActions.add(new ShowHideStatisticsConsolePanelAction(console));
			anActions.add(new ShowHideStatisticsStatusBarPanelAction(console));
		} else {
			anActions.add(new OpenConsoleSettingsAction(console));
		}
		anActions.addAll(Arrays.asList(super.postProcessPopupActions(console, actions)));
		replaceClearAction(anActions);
		return anActions.toArray(new AnAction[anActions.size()]);
	}

	private void replaceClearAction(ArrayList<AnAction> anActions) {
		for (int i = 0; i < anActions.size(); i++) {
			AnAction anAction = anActions.get(i);
			if (anAction instanceof ConsoleViewImpl.ClearAllAction) {
				anActions.set(i, clearAction());
			}
		}
	}

	private ConsoleViewImpl.ClearAllAction clearAction() {
		return new ConsoleViewImpl.ClearAllAction() {
			@Override
			public void actionPerformed(AnActionEvent e) {
				super.actionPerformed(e);
				final ConsoleView consoleView = e.getData(LangDataKeys.CONSOLE_VIEW);
				if (consoleView != null) {
                    try {
                        StatisticsManager.clearCount(consoleView);
                    } catch (Exception e1) {
                        //tough luck
                    }
                }
			}
		};
	}

}
