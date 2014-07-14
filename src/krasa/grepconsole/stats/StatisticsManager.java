package krasa.grepconsole.stats;

import java.awt.*;

import javax.swing.*;

import com.intellij.openapi.wm.impl.status.IdeStatusBarImpl;
import krasa.grepconsole.filter.GrepHighlightFilter;
import krasa.grepconsole.grep.GrepProcessor;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;
import krasa.grepconsole.plugin.ServiceManager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.*;

/**
 * @author Vojtech Krasa
 */
public class StatisticsManager {
	private static final Logger log = Logger.getInstance(StatisticsManager.class.getName());

	public static void createStatisticsPanels(final ConsoleViewImpl console) {
		GrepHighlightFilter highlightFilter = ServiceManager.getInstance().getHighlightFilter(console);
		if (highlightFilter == null) {
			log.error("highlightFilter is null");
			return;
		}

		Profile profile = GrepConsoleApplicationComponent.getInstance().getProfile(highlightFilter.getProject());
		if (profile.isShowStatsInStatusBarByDefault()) {
			createStatusBarPanel(console, highlightFilter);
		}

		if (profile.isShowStatsInConsoleByDefault()) {
			createConsolePanel(console, highlightFilter);
		}
	}

	public static void resetStatisticsPanels(ConsoleViewImpl consoleViewImpl) {
		resetConsolePanel(consoleViewImpl);
		resetStatusBarPanel(consoleViewImpl);
	}

	private static void resetStatusBarPanel(ConsoleViewImpl console) {
		GrepHighlightFilter highlightFilter = ServiceManager.getInstance().getHighlightFilter(console);
		if (highlightFilter == null) {
			log.error("highlightFilter is null");
			return;
		}
		GrepConsoleStatusBarWidget statusBarPanel = getStatusBarPanel(console);
		if (statusBarPanel != null) {
			StatisticsStatusBarPanel statisticsPanel = statusBarPanel.getStatisticsPanel();
			statisticsPanel.reset();
			if (!statisticsPanel.hasItems()) {
				statusBarPanel.dispose();
			} else {
				statisticsPanel.revalidate();
			}
		} else {
			Profile profile = GrepConsoleApplicationComponent.getInstance().getProfile(highlightFilter.getProject());
			if (profile.isShowStatsInStatusBarByDefault()) {
				createStatusBarPanel(console, highlightFilter);
			}
		}
	}

	public static void createStatusBarPanel(@NotNull ConsoleViewImpl consoleView,
			@NotNull GrepHighlightFilter highlightFilter) {
		final Project project = consoleView.getProject();
		final StatusBar statusBar = WindowManager.getInstance().getIdeFrame(project).getStatusBar();
		final GrepConsoleStatusBarWidget statusBarWidget = new GrepConsoleStatusBarWidget(consoleView, highlightFilter);
		statusBar.addWidget(statusBarWidget);
		statusBar.getComponent().revalidate();
		Disposer.register(consoleView, statusBarWidget);
	}

	public static void resetConsolePanel(ConsoleViewImpl consoleView) {
		GrepHighlightFilter highlightFilter = ServiceManager.getInstance().getHighlightFilter(consoleView);
		if (highlightFilter == null) {
			log.error("highlightFilter is null");
			return;
		}
		StatisticsConsolePanel statisticsConsolePanel = getConsolePanel(consoleView);
		if (statisticsConsolePanel != null) {
			statisticsConsolePanel.reset();
			if (!statisticsConsolePanel.hasItems()) {
				statisticsConsolePanel.dispose();
			} else {
				statisticsConsolePanel.revalidate();
			}
		} else {
			Profile profile = GrepConsoleApplicationComponent.getInstance().getProfile(consoleView.getProject());
			if (profile.isShowStatsInConsoleByDefault()) {
				createConsolePanel(consoleView, highlightFilter);
			}
		}
	}

	public static void createConsolePanel(@NotNull ConsoleViewImpl consoleView,
			@NotNull GrepHighlightFilter highlightFilter) {
		StatisticsConsolePanel statisticsConsolePanel = new StatisticsConsolePanel(highlightFilter);
		consoleView.add(statisticsConsolePanel, BorderLayout.SOUTH);
		consoleView.revalidate();
		Disposer.register(consoleView, statisticsConsolePanel);
	}

	@Nullable
	public static GrepConsoleStatusBarWidget getStatusBarPanel(@NotNull ConsoleViewImpl consoleView) {
		final Project project = consoleView.getProject();
		IdeFrame ideFrame = WindowManager.getInstance().getIdeFrame(project);
		final IdeStatusBarImpl statusBar = (IdeStatusBarImpl) ideFrame.getStatusBar();
		StatusBarWidget widget = statusBar.getWidget(GrepConsoleStatusBarWidget.createId(consoleView));
		return (GrepConsoleStatusBarWidget) widget;
	}

	@Nullable
	public static StatisticsConsolePanel getConsolePanel(JPanel consolePanel) {
		final BorderLayout layout = (BorderLayout) consolePanel.getLayout();
		final Component layoutComponent = layout.getLayoutComponent(BorderLayout.SOUTH);
		return (StatisticsConsolePanel) layoutComponent;
	}

	public static void clearCount(ConsoleView console) {
		final GrepHighlightFilter highlightFilter = ServiceManager.getInstance().getHighlightFilter(console);
		clearCount(highlightFilter);

	}

	public static void clearCount(GrepHighlightFilter highlightFilter) {
		if (highlightFilter != null) {
			for (GrepProcessor grepProcessor : highlightFilter.getGrepProcessors()) {
				grepProcessor.resetMatches();
			}
		}
	}

}