/*
 * $Id$
 *
 * Authors:
 *      Jeff Buchbinder <jeff@freemedsoftware.org>
 *      Jeremy Allen <ieziar.jeremy <--at--> gmail.com>
 *
 * FreeMED Electronic Medical Record and Practice Management System
 * Copyright (C) 1999-2012 FreeMED Software Foundation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package org.freemedsoftware.gwt.client.screen;

import static org.freemedsoftware.gwt.client.i18n.I18nUtil._;

import java.util.HashMap;
import java.util.Iterator;

import org.freemedsoftware.gwt.client.CurrentState;
import org.freemedsoftware.gwt.client.FreemedInterface;
import org.freemedsoftware.gwt.client.JsonUtil;
import org.freemedsoftware.gwt.client.ScreenInterface;
import org.freemedsoftware.gwt.client.SystemNotifications;
import org.freemedsoftware.gwt.client.Util;
import org.freemedsoftware.gwt.client.Util.ProgramMode;
import org.freemedsoftware.gwt.client.i18n.AppConstants;
import org.freemedsoftware.gwt.client.screen.entry.ClinicRegistrationEntry;
import org.freemedsoftware.gwt.client.widget.AccordionPanel;
import org.freemedsoftware.gwt.client.widget.CustomIFrame;
import org.freemedsoftware.gwt.client.widget.InfoDialog;
import org.freemedsoftware.gwt.client.widget.Toaster;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.HorizontalSplitPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class MainScreen extends Composite {

	public class MenuIcon extends Composite implements ClickHandler,
			MouseOverHandler, MouseOutHandler, HasClickHandlers,
			HasMouseOverHandlers, HasMouseOutHandlers {

		protected Command fireAction = null;

		public MenuIcon(Image icon, String caption, Command action) {
			VerticalPanel container = new VerticalPanel();
			container
					.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
			container.add(icon);

			Label captionWidget = new Label(caption);
			container.add(captionWidget);
			initWidget(container);

			// icon.addClickHandler(this);
			// captionWidget.addClickHandler(this);
			this.addClickHandler(this);

			// Over and out handlers
			this.addMouseOverHandler(this);
			this.addMouseOutHandler(this);
			this.setStyleName("accordion-item-unselected");

			fireAction = action;
		}

		public void onClick(ClickEvent event) {
			if (fireAction != null) {
				fireAction.execute();
			}
		}

		public void onMouseOver(MouseOverEvent event) {
			this.setStyleName("accordion-item-selected");
		}

		public void onMouseOut(MouseOutEvent event) {
			this.setStyleName("accordion-item-unselected");
		}

		public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
			return addDomHandler(handler, MouseOverEvent.getType());
		}

		public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
			return addDomHandler(handler, MouseOutEvent.getType());
		}

		public HandlerRegistration addClickHandler(ClickHandler handler) {
			return addDomHandler(handler, ClickEvent.getType());
		}
	}

	private static final AppConstants CONSTANTS = (AppConstants) GWT
			.create(AppConstants.class);

	private MenuBar menuBar_1;

	protected FreemedInterface freemedInterface;

	protected final DecoratedTabPanel tabPanel;

	protected final HorizontalSplitPanel statusBarContainer;

	protected final Label statusBar1, statusBar2;

	protected final Label loginUserInfo = new Label();

	protected final Label facilityInfo = new Label();
	// protected final CurrentState state = new CurrentState();

	protected final SystemNotifications notifications = new SystemNotifications();

	// protected final DashboardScreen dashboard = new DashboardScreen();

	protected final DashboardScreenNew dashboardScreenNew = DashboardScreenNew
			.getInstance();

	protected final HashMap<String, MenuIcon> leftNavMenuContainer = new HashMap<String, MenuIcon>();

	protected final VerticalPanel mainAccPanel;
	protected final VerticalPanel patientAccPanel;
	protected final VerticalPanel documentAccPanel;
	protected final VerticalPanel billingAccPanel;
	protected final VerticalPanel reportingAccPanel;
	protected final VerticalPanel utilitiesAccPanel;
	// protected final DecoratedStackPanel stackPanel;
	protected final AccordionPanel stackPanel;
	protected final HorizontalPanel shortCutsPanel;

	public MainScreen() {
		final DockPanel mainPanel = new DockPanel();
		initWidget(mainPanel);
		mainPanel.setSize("100%", "100%");

		// CurrentState.retrieveUserConfiguration(true);
		CurrentState.retrieveSystemConfiguration(true);

		// populateLeftNavigationPanel();

		JsonUtil.debug("MainScreen: call populateDefaultProvider");
		populateDefaultProvider();

		JsonUtil.debug("MainScreen: call populateDefaultFacility");
		JsonUtil
				.debug("MainScreen: assign object to CurrentState static object");
		CurrentState.assignMainScreen(this);

		/*
		 * Top Header panel to use top header shortcuts e.g logoff,add patient
		 * etc
		 */

		VerticalPanel topHeaderPanel = new VerticalPanel();
		topHeaderPanel.ensureDebugId("topHeaderPanel");
		topHeaderPanel.setStyleName(AppConstants.STYLE_HEADER_PANEL);
		topHeaderPanel.setWidth("100%");

		Image logoImage = new Image();
		logoImage.setUrl("resources/images/FreemedHeader.jpg");
		logoImage.setSize("100%", "55px");
		topHeaderPanel.add(logoImage);
		topHeaderPanel.setCellHorizontalAlignment(logoImage,
				HasHorizontalAlignment.ALIGN_CENTER);
		topHeaderPanel.setCellWidth(logoImage, "100%");

		HorizontalPanel topHeaderHorPanel = new HorizontalPanel();
		topHeaderHorPanel.setWidth("100%");

		HorizontalPanel facilityInfoPanel = new HorizontalPanel();
		facilityInfoPanel.setStyleName(AppConstants.STYLE_HEADER_PANEL);

		// adding userInfoPanel at top left
		HorizontalPanel userInfoPanel = new HorizontalPanel();
		userInfoPanel.setStyleName(AppConstants.STYLE_HEADER_PANEL);
		Image userImage = new Image();
		userImage.setUrl("resources/images/user-icon.png");
		userImage.setSize("13px", "100%");
		userInfoPanel.add(userImage);
		userInfoPanel.add(loginUserInfo);// Adding loginuserinfo link
		setLoginUserInfo();
		// Adding UserInfoPanel into top headerhorpanel
		HorizontalPanel hp = new HorizontalPanel();

		Image homeImage = new Image();
		homeImage.setUrl("resources/images/home-icon.png");
		homeImage.setSize("15px", "100%");
		facilityInfoPanel.add(homeImage);
		facilityInfoPanel.add(facilityInfo);

		HTML separator = new HTML("|");
		separator.setWidth("8px");
		separator.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

		hp.add(userInfoPanel);
		hp.add(separator);
		hp.add(facilityInfoPanel);
		// Adding UserInfoPanel into top headerhorpanel
		topHeaderHorPanel.add(hp);
		topHeaderHorPanel.setCellHorizontalAlignment(hp,
				HasHorizontalAlignment.ALIGN_LEFT);
		topHeaderHorPanel.setCellHorizontalAlignment(facilityInfoPanel,
				HasHorizontalAlignment.ALIGN_LEFT);
		// topHeaderHorPanel.setCellWidth(facilityInfoPanel, "20%");

		// adding shortcuts panel at top right corder

		shortCutsPanel = new HorizontalPanel();

		// Adding shortCutsPanel into top header
		topHeaderHorPanel.add(shortCutsPanel);
		topHeaderHorPanel.setCellHorizontalAlignment(shortCutsPanel,
				HasHorizontalAlignment.ALIGN_RIGHT);

		// Adding tophorpanel into top topheaderpanel
		topHeaderPanel.add(topHeaderHorPanel);
		topHeaderPanel.setCellWidth(topHeaderHorPanel, "100%");

		// Adding top header to main panel
		mainPanel.add(topHeaderPanel, DockPanel.NORTH);
		mainPanel.setCellWidth(topHeaderPanel, "100%");
		mainPanel.setCellHeight(topHeaderPanel, "3%");

		/*
		 * SimplePanel to hold (hopefully) a horizontal sub menu, going to try
		 * to use the Menu Bar items to call each sub-menu -JA
		 */

		JsonUtil.debug("MainScreen: create accordion panel");

		// Creating Left Navigation area with decorated stack panel
		stackPanel = new AccordionPanel();
		stackPanel.setWidth("100%");
		// stackPanel.setHeight("100%");

		{
			JsonUtil.debug("MainScreen: add main pane");
			mainAccPanel = new VerticalPanel();
			mainAccPanel.setStyleName("accordion-panel");
			mainAccPanel
					.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
			// adding Main Panel(System) into stack panel
			// stackPanel.add(mainAccPanel,
			// getHeaderString(AppConstants.SYSTEM_CATEGORY, null), true);

			JsonUtil.debug("MainScreen: add patient pane");
			patientAccPanel = new VerticalPanel();
			patientAccPanel.setStyleName("accordion-panel");
			patientAccPanel
					.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
			// adding Patient Panel into stack panel
			// stackPanel.add(patientAccPanel,
			// getHeaderString(AppConstants.PATIENT_CATEGORY, null), true);

			JsonUtil.debug("MainScreen: add document pane");
			documentAccPanel = new VerticalPanel();
			documentAccPanel.setStyleName("accordion-panel");
			documentAccPanel
					.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
			// adding Documents Panel into stack panel
			// stackPanel.add(documentAccPanel,
			// getHeaderString(AppConstants.DOCUMENTS_CATEGORY, null), true);
			JsonUtil.debug("MainScreen: add Billing pane");
			billingAccPanel = new VerticalPanel();
			billingAccPanel.setStyleName("accordion-panel");
			billingAccPanel
					.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
			// adding Billing Panel into stack panel
			// stackPanel.add(billingAccPanel,
			// getHeaderString(AppConstants.BILLING_CATEGORY, null), true);

			JsonUtil.debug("MainScreen: add Reporting pane");
			reportingAccPanel = new VerticalPanel();
			reportingAccPanel.setStyleName("accordion-panel");
			reportingAccPanel
					.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
			// adding Reporting Panel into stack panel
			// stackPanel.add(reportingAccPanel,
			// getHeaderString(AppConstants.REPORTING_CATEGORY, null), true);

			JsonUtil.debug("MainScreen: add utilities pane");
			utilitiesAccPanel = new VerticalPanel();
			utilitiesAccPanel.setStyleName("accordion-panel");
			utilitiesAccPanel
					.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

			// adding Utilities Panel into stack panel
			// stackPanel.add(utilitiesAccPanel,
			// getHeaderString(AppConstants.UTILITIES_CATEGORY, null), true);

			// Disable for now
			// accordionPanel.selectPanel("Main");
		}
		JsonUtil
				.debug("MainScreen: create container hpanel for accordion and tabs");

		HorizontalPanel menuAndContent = new HorizontalPanel();
		menuAndContent.setSize("100%", "100%");

		// Jam them together, no space.
		menuAndContent.setSpacing(0);
		// menuAndContent.setCellWidth(accordionPanel, "250px");

		JsonUtil.debug("MainScreen: add accordion and tab panel to container");
		menuAndContent.add(stackPanel);
		stackPanel.ensureDebugId("cwStackPanel");

		// defining left navigation area width
		menuAndContent.setCellWidth(stackPanel, "12%");

		JsonUtil.debug("MainScreen: create tabPanel");
		tabPanel = new DecoratedTabPanel();

		tabPanel.setSize("100%", "100%");
		tabPanel.setAnimationEnabled(true);
		menuAndContent.add(tabPanel);
		// defining content area width
		menuAndContent.setCellWidth(tabPanel, "88%");

		menuAndContent.setCellHorizontalAlignment(tabPanel,
				HasHorizontalAlignment.ALIGN_LEFT);
		JsonUtil.debug("MainScreen: add container to dock panel");
		mainPanel.add(menuAndContent, DockPanel.CENTER);

		JsonUtil.debug("MainScreen: add dashboard panel to tabs and select");
		// tabPanel.add(dashboard, "Dashboard");
		tabPanel.add(dashboardScreenNew, "Dashboard");

		tabPanel.selectTab(0);
		JsonUtil.debug("MainScreen: pass tabPanel to static CurrentState");
		CurrentState.assignTabPanel(tabPanel);

		// Get configuration
		CurrentState.retrieveUserConfiguration(true, new Command() {
			public void execute() {
				JsonUtil.debug("MainScreen: Set State of dashboard");
				dashboardScreenNew.loadWidgets();
				dashboardScreenNew.reloadDashboard();
			}
		});

		// Expand out main tabpanel to take up all extra room
		JsonUtil.debug("MainScreen: expand tabpanel");
		// mainPanel.setCellWidth(tabPanel, "100%");
		// mainPanel.setCellHeight(tabPanel, "100%");

		final HTML poweredByLabel = new HTML(_("Powered By FreeMED&trade;"));
		poweredByLabel.setStyleName(AppConstants.STYLE_LABEL_SMALL);
		mainPanel.add(poweredByLabel, DockPanel.SOUTH);
		mainPanel.setCellHorizontalAlignment(poweredByLabel,
				HasHorizontalAlignment.ALIGN_CENTER);

		JsonUtil.debug("MainScreen: split panel");
		statusBarContainer = new HorizontalSplitPanel();
		mainPanel.add(statusBarContainer, DockPanel.SOUTH);
		statusBarContainer.setSize("100%", "30px");
		statusBarContainer.setSplitPosition("50%");

		JsonUtil.debug("MainScreen: status bar");
		statusBar1 = new Label(_("Ready"));
		statusBar1.setStyleName("statusBar");
		statusBarContainer.add(statusBar1);
		CurrentState.assignStatusBar(statusBar1);
		statusBar2 = new Label("-");
		statusBar2.setStyleName("statusBar");
		statusBarContainer.add(statusBar2);
		if (Util.isStubbedMode()) {
			statusBar2.setText("STUBBED / TEST MODE");
		}
		populateDefaultFacility();

		// Create notification toaster
		JsonUtil.debug("MainScreen: create toaster");
		if (CurrentState.getToaster() == null) {
			Toaster toaster = new Toaster();
			CurrentState.assignToaster(toaster);
			toaster.setTimeout(7);
		}

		// Handle system notifications
		// notifications.setState(getCurrentState());
		JsonUtil.debug("MainScreen: start notifications");
		notifications.start();

		if (Util.getProgramMode() == ProgramMode.STUBBED)
			initNavigations();

		tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
			@Override
			public void onSelection(SelectionEvent<Integer> arg0) {
				if (tabPanel.getWidget(arg0.getSelectedItem()) instanceof ScreenInterface) {
					ScreenInterface screenInterface = ((ScreenInterface) tabPanel
							.getWidget(arg0.getSelectedItem()));
					String className = screenInterface.getClass().getName();
					className = className
							.substring(className.lastIndexOf('.') + 1);
					CurrentState.assignCurrentPageHelp(className);
				}
			}
		});

		// Force showing the screen
		// show();
	}

	protected void initTopHeaderNavPanel() {

		shortCutsPanel.clear();

		shortCutsPanel.setStyleName(AppConstants.STYLE_HEADER_PANEL);
		// adding current page help link
		PushButton helpButton = new PushButton();
		helpButton.setTitle(_("help"));
		helpButton.setStyleName(AppConstants.STYLE_BUTTON_SIMPLE);
		helpButton.getUpFace().setImage(
				new Image(GWT.getHostPageBaseURL()
						+ "resources/images/help.16x16.png"));
		helpButton.getDownFace().setImage(
				new Image(GWT.getHostPageBaseURL()
						+ "resources/images/help.16x16.png"));
		helpButton.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				if (Util.getProgramMode() == ProgramMode.STUBBED) {
					Window.alert("Running in stubbed mode");
				} else if (Util.getProgramMode() == ProgramMode.JSONRPC) {
					CustomIFrame customIFrame = new CustomIFrame(Util
							.getHelpRequest(),
							(int) (Window.getClientWidth() * .60),
							(int) (Window.getClientHeight() * .60));
					shortCutsPanel.add(customIFrame);
					customIFrame.show();
					customIFrame.center();
				}
			}
		});
		Util.attachHelp(helpButton, _("Help Icon"),
				_("Help for the currently selected tab."), true);
		shortCutsPanel.add(helpButton);

		HTML separator = null;
		if (CurrentState.isMenuAllowed(AppConstants.SYSTEM_CATEGORY,
				AppConstants.SCHEDULER)) {
			// Adding spacer
			separator = new HTML("|");
			separator.setWidth("8px");
			separator
					.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
			shortCutsPanel.add(separator);

			// adding scheduler link
			PushButton schedulerButton = new PushButton();
			schedulerButton.setTitle(_("scheduler"));
			schedulerButton.setStyleName(AppConstants.STYLE_BUTTON_SIMPLE);
			schedulerButton.getUpFace().setImage(
					new Image(GWT.getHostPageBaseURL()
							+ "resources/images/scheduler.16x16.png"));
			schedulerButton.getDownFace().setImage(
					new Image(GWT.getHostPageBaseURL()
							+ "resources/images/scheduler.16x16.png"));
			schedulerButton.addClickHandler(new ClickHandler() {

				public void onClick(ClickEvent event) {
					Util.spawnTab(AppConstants.SCHEDULER, SchedulerScreen
							.getInstance());
				}
			});
			Util.attachHelp(schedulerButton, _("Scheduler"),
					_("View and book appointments."), true);
			shortCutsPanel.add(schedulerButton);
		}

		if (CurrentState.isMenuAllowed(AppConstants.PATIENT_CATEGORY,
				AppConstants.SEARCH)) {
			// Adding spacer
			separator = new HTML("|");
			separator.setWidth("8px");
			separator
					.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
			shortCutsPanel.add(separator);

			// adding Patient Search Link
			PushButton ptSearchButton = new PushButton();
			ptSearchButton.setTitle(_("search patient"));
			ptSearchButton.setStyleName(AppConstants.STYLE_BUTTON_SIMPLE);
			ptSearchButton.getUpFace().setImage(
					new Image(GWT.getHostPageBaseURL()
							+ "resources/images/patient_icon.16x16.png"));
			ptSearchButton.getDownFace().setImage(
					new Image(GWT.getHostPageBaseURL()
							+ "resources/images/patient_icon.16x16.png"));
			ptSearchButton.addClickHandler(new ClickHandler() {

				public void onClick(ClickEvent event) {
					Util.spawnTab(AppConstants.SEARCH, PatientSearchScreen
							.getInstance());
				}
			});
			Util.attachHelp(ptSearchButton, _("Patient Search"),
					_("Search for patients."), true);
			shortCutsPanel.add(ptSearchButton);
		}
		// Adding spacer
		separator = new HTML("|");
		separator.setWidth("8px");
		separator.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		shortCutsPanel.add(separator);
		// Adding preferences link
		PushButton preferencesButton = new PushButton();
		preferencesButton.setTitle(_("preferences"));
		preferencesButton.setStyleName(AppConstants.STYLE_BUTTON_SIMPLE);
		preferencesButton.getUpFace().setImage(
				new Image(GWT.getHostPageBaseURL()
						+ "resources/images/preferences.16x16.png"));
		preferencesButton.getDownFace().setImage(
				new Image(GWT.getHostPageBaseURL()
						+ "resources/images/preferences.16x16.png"));
		preferencesButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				// logout event when clicked on logout link
				// if (Util.getProgramMode() == ProgramMode.STUBBED) {
				// Window.alert("Running in stubbed mode");
				// } else if (Util.getProgramMode() == ProgramMode.JSONRPC) {
				final PreferencesScreen preferencesScreen = PreferencesScreen
						.getInstance();
				Util.spawnTab(_("Preferences"), preferencesScreen);
				// }
			}
		});
		Util.attachHelp(preferencesButton, _("Preferences"),
				_("Set your user prefereneces."), true);
		shortCutsPanel.add(preferencesButton);
		separator = new HTML("|");
		separator.setWidth("8px");
		separator.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		shortCutsPanel.add(separator);

		// Adding support link
		PushButton supportButton = new PushButton();
		supportButton.setTitle(_("support"));
		supportButton.setStyleName(AppConstants.STYLE_BUTTON_SIMPLE);
		supportButton.getUpFace().setImage(
				new Image(GWT.getHostPageBaseURL()
						+ "resources/images/support.16x16.png"));
		supportButton.getDownFace().setImage(
				new Image(GWT.getHostPageBaseURL()
						+ "resources/images/support.16x16.png"));
		supportButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				InfoDialog d = new InfoDialog();
				d.setCaption("Support");
				d
						.setContent(new HTML(
								"Commercial support is available for <b>FreeMED</b> through the Foundation's commercial support partners."
										+ "<br/><br/>"
										+ "More information is available at <a href=\"http://freemedsoftware.org/commercial_support\" target=\"_new\">http://freemedsoftware.org/commercial_support</a>."
										+ "<br/><br/>"
										+ "<hr/>"
										+ "<br/></br>"
										+ "Community support is available on the FreeMED group at "
										+ "<a href=\"http://groups.google.com/group/freemed-support?hl=en\">http://groups.google.com/group/freemed-support?hl=en</a>."));
				d.center();
			}
		});
		Util.attachHelp(supportButton, _("Support"),
				_("More information about support."), true);
		shortCutsPanel.add(supportButton);
		// Adding logout link
		separator = new HTML("|");
		separator.setWidth("8px");
		separator.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		shortCutsPanel.add(separator);

		// Adding logout link
		PushButton logoutButton = new PushButton();
		logoutButton.setTitle(_("logout"));
		logoutButton.setStyleName(AppConstants.STYLE_BUTTON_SIMPLE);
		logoutButton.getUpFace().setImage(
				new Image(GWT.getHostPageBaseURL()
						+ "resources/images/logout.16x16.png"));
		logoutButton.getDownFace().setImage(
				new Image(GWT.getHostPageBaseURL()
						+ "resources/images/logout.16x16.png"));
		logoutButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				// logout event when clicked on logout link
				if (Util.getProgramMode() == ProgramMode.STUBBED) {
					Window.alert("Running in stubbed mode");
				} else if (Util.getProgramMode() == ProgramMode.JSONRPC) {
					// dashboard.saveArrangement();
					Util.logout(new Command() {
						@Override
						public void execute() {
							// TODO Auto-generated method stub
							notifications.stop();
						}
					});
				}
			}
		});
		Util.attachHelp(logoutButton, _("Logout"), _("Logout of FreeMED."), true);
		shortCutsPanel.add(logoutButton);
	}

	public MainScreen getMainScreen() {
		return this;
	}

	public void initMainScreen() {
		initNavigations();
		CurrentState.CUR_THEME = CurrentState.getUserConfig(
				AppConstants.SYSTEM_THEME, "String").toString();
		Util.updateStyleSheets(CurrentState.CUR_THEME, CurrentState.LAST_THEME);
		CurrentState.assignSYSTEM_NOTIFY_TYPE(CurrentState.getUserConfig(
				AppConstants.SYSTEM_NOTIFICATION, "String").toString());
		populateDefaultFacility();
	}

	public void initNavigations() {

		// Initializing to header navigations
		initTopHeaderNavPanel();

		JsonUtil.debug("MainScreen.initNavigations: start");
		emptyLeftNavMenuContainer();
		/*
		 * Start Adding System panel options
		 */
		boolean showSystemPanel = false;
		HashMap<String, HashMap<String, Integer>> leftNavigationMenu = CurrentState
				.getLeftNavigationOptions();
		if (leftNavigationMenu.get(AppConstants.SYSTEM_CATEGORY) != null
				|| Util.getProgramMode() == ProgramMode.STUBBED) {
			if (isNeedToCreate(AppConstants.SYSTEM_CATEGORY,
					AppConstants.DASHBOARD)) {
				showSystemPanel = true;
				MenuIcon menuIcon = new MenuIcon(new Image(GWT
						.getHostPageBaseURL()
						+ "resources/images/dashboard.32x32.png"),
						AppConstants.DASHBOARD, new Command() {
							public void execute() {
								Util.spawnTab(AppConstants.DASHBOARD,
										dashboardScreenNew);
							}
						});
				leftNavMenuContainer.put(AppConstants.DASHBOARD, menuIcon);
				mainAccPanel.add(menuIcon);
			}

			if (isNeedToCreate(AppConstants.SYSTEM_CATEGORY,
					AppConstants.SCHEDULER)) {
				showSystemPanel = true;
				MenuIcon menuIcon = new MenuIcon(new Image(GWT
						.getHostPageBaseURL()
						+ "resources/images/scheduler.32x32.png"),
						AppConstants.SCHEDULER, new Command() {
							public void execute() {
								Util.spawnTab(AppConstants.SCHEDULER,
										SchedulerScreen.getInstance());
							}
						});
				leftNavMenuContainer.put(AppConstants.SCHEDULER, menuIcon);
				mainAccPanel.add(menuIcon);
			}
			if (isNeedToCreate(AppConstants.SYSTEM_CATEGORY,
					AppConstants.MESSAGES)) {
				showSystemPanel = true;
				MenuIcon menuIcon = new MenuIcon(new Image(GWT
						.getHostPageBaseURL()
						+ "resources/images/messaging.32x32.png"),
						AppConstants.MESSAGES, new Command() {
							public void execute() {
								Util.spawnTab(AppConstants.MESSAGES,
										MessagingScreen.getInstance());
							}
						});
				leftNavMenuContainer.put(AppConstants.MESSAGES, menuIcon);
				mainAccPanel.add(menuIcon);
			}
		}

		// Clinic Registration
		if (true) {
			showSystemPanel = true;
			MenuIcon menuIcon = new MenuIcon(new Image(GWT.getHostPageBaseURL()
					+ "resources/images/add_plus.32x32.png"),
					AppConstants.CLINIC_REGISTRATION, new Command() {
						public void execute() {
							ClinicRegistrationEntry clinicRegistrationEntry = new ClinicRegistrationEntry();
							Util.spawnTab(AppConstants.CLINIC_REGISTRATION,
									clinicRegistrationEntry);
						}
					});
			leftNavMenuContainer
					.put(AppConstants.CLINIC_REGISTRATION, menuIcon);
			mainAccPanel.add(menuIcon);
		}
		
		// Triage
		if (true) {
			showSystemPanel = true;
			MenuIcon menuIcon = new MenuIcon(new Image(GWT.getHostPageBaseURL()
					+ "resources/images/doc_referout.32x32.png"),
					AppConstants.TRIAGE, new Command() {
						public void execute() {
							TriageScreen triageScreen = new TriageScreen();
							Util.spawnTab(AppConstants.TRIAGE,
									triageScreen);
						}
					});
			leftNavMenuContainer
					.put(AppConstants.TRIAGE, menuIcon);
			mainAccPanel.add(menuIcon);
		}

		

		if (showSystemPanel)
			addWighetToStack(mainAccPanel, AppConstants.SYSTEM_CATEGORY);

		// ////////////////////////////////End Adding System panel options
		// ///////////////////////////

		/*
		 * Start Adding Patient panel options
		 */
		boolean showPatientPanel = false;
		if (leftNavigationMenu.get(AppConstants.PATIENT_CATEGORY) != null
				|| Util.getProgramMode() == ProgramMode.STUBBED) {
			if (isNeedToCreate(AppConstants.PATIENT_CATEGORY,
					AppConstants.SEARCH)) {
				showPatientPanel = true;
				MenuIcon menuIcon = new MenuIcon(new Image(GWT
						.getHostPageBaseURL()
						+ "resources/images/patient_search.32x32.png"),
						AppConstants.SEARCH, new Command() {
							public void execute() {
								Util.spawnTab(AppConstants.SEARCH,
										PatientSearchScreen.getInstance());
							}
						});
				leftNavMenuContainer.put(AppConstants.SEARCH, menuIcon);
				patientAccPanel.add(menuIcon);
			}
			if (isNeedToCreate(AppConstants.PATIENT_CATEGORY,
					AppConstants.NEW_PATIENT)) {
				showPatientPanel = true;
				MenuIcon menuIcon = new MenuIcon(new Image(GWT
						.getHostPageBaseURL()
						+ "resources/images/patient_entry.32x32.png"),
						AppConstants.NEW_PATIENT, new Command() {
							public void execute() {
								PatientForm patientForm = PatientForm
										.getInstance();
								patientForm.setPrimaryFacility(CurrentState
										.getDefaultFacility());
								Util.spawnTab(AppConstants.NEW_PATIENT,
										patientForm);
							}
						});
				leftNavMenuContainer.put(AppConstants.NEW_PATIENT, menuIcon);
				patientAccPanel.add(menuIcon);
			}
			if (isNeedToCreate(AppConstants.PATIENT_CATEGORY,
					AppConstants.GROUPS)) {
				showPatientPanel = true;
				MenuIcon menuIcon = new MenuIcon(new Image(GWT
						.getHostPageBaseURL()
						+ "resources/images/callin.32x32.png"),
						AppConstants.GROUPS, new Command() {
							public void execute() {
								Util.spawnTab(AppConstants.GROUPS,
										PatientsGroupScreen.getInstance());
							}
						});
				leftNavMenuContainer.put(AppConstants.GROUPS, menuIcon);
				patientAccPanel.add(menuIcon);
			}
			if (isNeedToCreate(AppConstants.PATIENT_CATEGORY,
					AppConstants.CALL_IN)) {
				showPatientPanel = true;
				MenuIcon menuIcon = new MenuIcon(new Image(GWT
						.getHostPageBaseURL()
						+ "resources/images/callin.32x32.png"),
						AppConstants.CALL_IN, new Command() {
							public void execute() {
								Util.spawnTab(AppConstants.CALL_IN,
										CallInScreen.getInstance());
							}
						});
				leftNavMenuContainer.put(AppConstants.CALL_IN, menuIcon);
				patientAccPanel.add(menuIcon);
			}
			if (isNeedToCreate(AppConstants.PATIENT_CATEGORY,
					AppConstants.RX_REFILL)) {
				showPatientPanel = true;
				MenuIcon menuIcon = new MenuIcon(new Image(GWT
						.getHostPageBaseURL()
						+ "resources/images/rx_refill.32x32.png"), "Rx Refill",
						new Command() {
							public void execute() {
								Util.spawnTab(AppConstants.RX_REFILL,
										RxRefillScreen.getInstance());
							}
						});
				leftNavMenuContainer.put(AppConstants.RX_REFILL, menuIcon);
				patientAccPanel.add(menuIcon);
			}
			if (isNeedToCreate(AppConstants.PATIENT_CATEGORY,
					AppConstants.TAG_SEARCH)) {
				showPatientPanel = true;
				MenuIcon menuIcon = new MenuIcon(new Image(GWT
						.getHostPageBaseURL()
						+ "resources/images/patient.32x32.png"),
						AppConstants.TAG_SEARCH, new Command() {
							public void execute() {
								Util.spawnTab(AppConstants.TAG_SEARCH,
										PatientTagSearchScreen.getInstance());
							}
						});
				leftNavMenuContainer.put(AppConstants.TAG_SEARCH, menuIcon);
				patientAccPanel.add(menuIcon);
			}
		}

		if (showPatientPanel)
			addWighetToStack(patientAccPanel, AppConstants.PATIENT_CATEGORY);

		// ////////////////////////////////End Adding Patient panel options
		// ///////////////////////////

		/*
		 * Start Adding Documents panel options
		 */
		boolean showDocumentPanel = false;
		if (leftNavigationMenu.get(AppConstants.DOCUMENTS_CATEGORY) != null
				|| Util.getProgramMode() == ProgramMode.STUBBED) {
			if (isNeedToCreate(AppConstants.DOCUMENTS_CATEGORY,
					AppConstants.UNFILED)) {
				showDocumentPanel = true;
				MenuIcon menuIcon = new MenuIcon(new Image(GWT
						.getHostPageBaseURL()
						+ "resources/images/unfiled.32x32.png"),
						AppConstants.UNFILED, new Command() {
							public void execute() {
								Util.spawnTab(AppConstants.UNFILED
										+ " Documents", UnfiledDocuments
										.getInstance());
							}
						});
				leftNavMenuContainer.put(AppConstants.UNFILED, menuIcon);
				documentAccPanel.add(menuIcon);
			}
			if (isNeedToCreate(AppConstants.DOCUMENTS_CATEGORY,
					AppConstants.UNREAD)) {
				showDocumentPanel = true;
				MenuIcon menuIcon = new MenuIcon(new Image(GWT
						.getHostPageBaseURL()
						+ "resources/images/unread.32x32.png"),
						AppConstants.UNREAD, new Command() {
							public void execute() {
								Util.spawnTab(AppConstants.UNREAD
										+ " Documents", UnreadDocuments
										.getInstance());
							}
						});
				leftNavMenuContainer.put(AppConstants.UNREAD, menuIcon);
				documentAccPanel.add(menuIcon);
			}
		}

		if (showDocumentPanel)
			addWighetToStack(documentAccPanel, AppConstants.DOCUMENTS_CATEGORY);

		// ////////////////////////////////End Adding Documents panel options
		// ///////////////////////////

		/*
		 * Start Adding Billing panel options
		 */
		boolean showBillingPanel = false;
		if (leftNavigationMenu.get(AppConstants.BILLING_CATEGORY) != null
				|| Util.getProgramMode() == ProgramMode.STUBBED) {
			if (isNeedToCreate(AppConstants.BILLING_CATEGORY,
					AppConstants.ACCOUNT_RECEIVABLE)) {
				showBillingPanel = true;
				MenuIcon menuIcon = new MenuIcon(new Image(GWT
						.getHostPageBaseURL()
						+ "resources/images/accounts_receivableNew.32x32.png"),
						AppConstants.ACCOUNT_RECEIVABLE, new Command() {
							public void execute() {
								Util.spawnTab(AppConstants.ACCOUNT_RECEIVABLE,
										AccountsReceivableScreen.getInstance());
							}
						});
				leftNavMenuContainer.put(AppConstants.ACCOUNT_RECEIVABLE,
						menuIcon);
				billingAccPanel.add(menuIcon);
			}
			if (isNeedToCreate(AppConstants.BILLING_CATEGORY,
					AppConstants.CLAIMS_MANAGER)) {
				showBillingPanel = true;
				MenuIcon menuIcon = new MenuIcon(new Image(GWT
						.getHostPageBaseURL()
						+ "resources/images/claims_manage.32x32.png"),
						AppConstants.CLAIMS_MANAGER, new Command() {
							public void execute() {
								Util.spawnTab(AppConstants.CLAIMS_MANAGER,
										ClaimsManager.getInstance());
							}
						});
				leftNavMenuContainer.put(AppConstants.CLAIMS_MANAGER, menuIcon);
				billingAccPanel.add(menuIcon);
			}
			if (isNeedToCreate(AppConstants.BILLING_CATEGORY,
					AppConstants.REMITT_BILLING)) {
				showBillingPanel = true;
				MenuIcon menuIcon = new MenuIcon(new Image(GWT
						.getHostPageBaseURL()
						+ "resources/images/remitt.32x32.png"),
						AppConstants.REMITT_BILLING, new Command() {
							public void execute() {
								Util.spawnTab(AppConstants.REMITT_BILLING,
										RemittBilling.getInstance());
							}
						});
				leftNavMenuContainer.put(AppConstants.REMITT_BILLING, menuIcon);
				billingAccPanel.add(menuIcon);
			}
			if (isNeedToCreate(AppConstants.BILLING_CATEGORY,
					AppConstants.SUPER_BILLS)) {
				showBillingPanel = true;
				MenuIcon menuIcon = new MenuIcon(new Image(GWT
						.getHostPageBaseURL()
						+ "resources/images/superbill.32x32.png"),
						AppConstants.SUPER_BILLS, new Command() {
							public void execute() {
								Util.spawnTab(AppConstants.SUPER_BILLS,
										SuperBills.getInstance());
							}
						});
				leftNavMenuContainer.put(AppConstants.SUPER_BILLS, menuIcon);
				billingAccPanel.add(menuIcon);
			}
		}

		if (showBillingPanel)
			addWighetToStack(billingAccPanel, AppConstants.BILLING_CATEGORY);

		// ////////////////////////////////End Adding Billing panel options
		// ///////////////////////////

		/*
		 * Start Adding Reporting panel options
		 */
		boolean showReportingPanel = false;
		if (leftNavigationMenu.get(AppConstants.REPORTING_CATEGORY) != null
				|| Util.getProgramMode() == ProgramMode.STUBBED) {
			if (isNeedToCreate(AppConstants.REPORTING_CATEGORY,
					AppConstants.REPORTING_ENGINE)) {
				showReportingPanel = true;
				MenuIcon menuIcon = new MenuIcon(new Image(GWT
						.getHostPageBaseURL()
						+ "resources/images/reporting.32x32.png"),
						AppConstants.REPORTING_ENGINE, new Command() {
							public void execute() {
								Util.spawnTab(AppConstants.REPORTING_ENGINE,
										ReportingScreen.getInstance());
							}
						});
				leftNavMenuContainer.put(AppConstants.REPORTING_ENGINE,
						menuIcon);
				reportingAccPanel.add(menuIcon);
			}
			if (isNeedToCreate(AppConstants.REPORTING_CATEGORY,
					AppConstants.REPORTING_LOG)) {
				showReportingPanel = true;
				MenuIcon menuIcon = new MenuIcon(new Image(GWT
						.getHostPageBaseURL()
						+ "resources/images/reporting.32x32.png"),
						AppConstants.REPORTING_LOG, new Command() {
							public void execute() {
								Util.spawnTab(AppConstants.REPORTING_LOG,
										ReportingLogScreen.getInstance());
							}
						});
				leftNavMenuContainer.put(AppConstants.REPORTING_LOG, menuIcon);
				reportingAccPanel.add(menuIcon);
			}
		}

		if (showReportingPanel)
			addWighetToStack(reportingAccPanel, AppConstants.REPORTING_CATEGORY);

		// ////////////////////////////////End Adding Reporting panel options
		// ///////////////////////////

		/*
		 * Start Adding Utilities panel options
		 */
		boolean showUtilitiesPanel = false;
		if (leftNavigationMenu.get(AppConstants.UTILITIES_CATEGORY) != null
				|| Util.getProgramMode() == ProgramMode.STUBBED) {
			if (isNeedToCreate(AppConstants.UTILITIES_CATEGORY,
					AppConstants.TOOLS_SCREEN)) {
				showUtilitiesPanel = true;
				MenuIcon menuIcon = new MenuIcon(new Image(GWT
						.getHostPageBaseURL()
						+ "resources/images/tools.32x32.png"),
						AppConstants.TOOLS_SCREEN, new Command() {
							public void execute() {
								Util.spawnTab(AppConstants.TOOLS_SCREEN,
										ToolsScreen.getInstance());
							}
						});
				leftNavMenuContainer.put(AppConstants.TOOLS_SCREEN, menuIcon);
				utilitiesAccPanel.add(menuIcon);
			}
			if (isNeedToCreate(AppConstants.UTILITIES_CATEGORY,
					AppConstants.SUPPORT_DATA)) {
				showUtilitiesPanel = true;
				MenuIcon menuIcon = new MenuIcon(new Image(GWT
						.getHostPageBaseURL()
						+ "resources/images/modules.32x32.png"),
						AppConstants.SUPPORT_DATA, new Command() {
							public void execute() {
								Util.spawnTab(AppConstants.SUPPORT_DATA,
										SupportDataScreen.getInstance());
							}
						});
				leftNavMenuContainer.put(AppConstants.SUPPORT_DATA, menuIcon);
				utilitiesAccPanel.add(menuIcon);
			}
			if (isNeedToCreate(AppConstants.UTILITIES_CATEGORY,
					AppConstants.FIELD_CHECKER)) {
				showUtilitiesPanel = true;
				MenuIcon menuIcon = new MenuIcon(new Image(GWT
						.getHostPageBaseURL()
						+ "resources/images/modules.32x32.png"),
						AppConstants.FIELD_CHECKER, new Command() {
							public void execute() {
								Util.spawnTab(AppConstants.FIELD_CHECKER,
										ModuleFieldCheckerTypeEntryScreen
												.getInstance());
								ModuleFieldCheckerTypeEntryScreen.getInstance()
										.retrieveAndFillListData();
							}
						});
				leftNavMenuContainer.put(AppConstants.FIELD_CHECKER, menuIcon);
				utilitiesAccPanel.add(menuIcon);
			}
			if (isNeedToCreate(AppConstants.UTILITIES_CATEGORY,
					AppConstants.USER_MANAGEMENT)) {
				showUtilitiesPanel = true;
				MenuIcon menuIcon = new MenuIcon(new Image(GWT
						.getHostPageBaseURL()
						+ "resources/images/user_config.32x32.png"),
						AppConstants.USER_MANAGEMENT, new Command() {
							public void execute() {
								Util.spawnTab(AppConstants.USER_MANAGEMENT,
										UserManagementScreen.getInstance());
							}
						});
				leftNavMenuContainer
						.put(AppConstants.USER_MANAGEMENT, menuIcon);
				utilitiesAccPanel.add(menuIcon);
			}
			if (isNeedToCreate(AppConstants.UTILITIES_CATEGORY,
					AppConstants.SYSTEM_CONFIGURATION)) {
				showUtilitiesPanel = true;
				MenuIcon menuIcon = new MenuIcon(new Image(GWT
						.getHostPageBaseURL()
						+ "resources/images/settings.32x32.png"),
						AppConstants.SYSTEM_CONFIGURATION, new Command() {
							public void execute() {
								Util.spawnTab(
										AppConstants.SYSTEM_CONFIGURATION,
										ConfigurationScreen.getInstance());
							}
						});
				leftNavMenuContainer.put(AppConstants.SYSTEM_CONFIGURATION,
						menuIcon);
				utilitiesAccPanel.add(menuIcon);
			}
			/*
			 * if (isNeedToCreate(AppConstants.UTILITIES_CATEGORY,
			 * AppConstants.DB_ADMINISTRATION)) { showUtilitiesPanel = true;
			 * MenuIcon menuIcon = new MenuIcon(new Image(GWT
			 * .getHostPageBaseURL() + "/resources/images/settings.32x32.png"),
			 * AppConstants.DB_ADMINISTRATION, new Command() { public void
			 * execute() { Util.spawnTab(AppConstants.DB_ADMINISTRATION, null);
			 * } }); leftNavMenuContainer.put(AppConstants.DB_ADMINISTRATION,
			 * menuIcon); utilitiesAccPanel.add(menuIcon); }
			 */
			if (isNeedToCreate(AppConstants.UTILITIES_CATEGORY,
					AppConstants.ACL)) {
				showUtilitiesPanel = true;
				MenuIcon menuIcon = new MenuIcon(new Image(GWT
						.getHostPageBaseURL()
						+ "resources/images/settings.32x32.png"),
						AppConstants.ACL, new Command() {
							public void execute() {
								Util.spawnTab(AppConstants.ACL, ACLScreen
										.getInstance());
							}
						});
				leftNavMenuContainer.put(AppConstants.ACL, menuIcon);
				utilitiesAccPanel.add(menuIcon);
			}
		}

		if (showUtilitiesPanel)
			addWighetToStack(utilitiesAccPanel, AppConstants.UTILITIES_CATEGORY);
		// ////////////////////////////////End Adding utilities panel options
		// ///////////////////////////
		stackPanel.selectPanel(0);// showStack(0);//seselectPanel(0);
		JsonUtil.debug("MainScreen.initNavigations: ends");
	}

	public void removeWighetFromStack(Widget widget) {
		stackPanel.remove(widget);
		// stackPanel.selectPanel(0);
	}

	public void addWighetToStack(Widget widget, String title) {
		/*
		 * if (stackPanel.getWidgetIndex(widget) == -1)
		 * stackPanel.add(widget,getHeaderString(title,null),true);
		 */
		if (stackPanel.getPanelIndex(widget) == -1)
			stackPanel.add(title, widget);
	}

	/*
	 * evaluate whether this menu option should be visible or not
	 * 
	 * @param title of the navigation option
	 */
	public boolean isNeedToCreate(String menuCatagory, String title) {
		if (CurrentState.isMenuAllowed(menuCatagory, title))
			return true;
		if (leftNavMenuContainer.get(title) != null) {
			MenuIcon menuIcon = leftNavMenuContainer.get(title);
			menuIcon.removeFromParent();
			menuIcon = null;
			leftNavMenuContainer.remove(title);
		}

		return false;
	}

	private String getHeaderString(String text, AbstractImagePrototype image) {
		// Add the image and text to a horizontal panel
		HorizontalPanel hPanel = new HorizontalPanel();
		hPanel.setSpacing(0);
		hPanel.setWidth("100%");
		hPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		hPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		if (image != null)
			hPanel.add(image.createImage());
		HTML headerText = new HTML(text);
		headerText.setStyleName("stackPanelHeader");
		hPanel.add(headerText);

		// Return the HTML string for the panel
		return hPanel.getElement().getString();
	}

	public void setLoginUserInfo() {
		if (Util.getProgramMode() == ProgramMode.STUBBED) {
			// TODO stubbed mode goes here
			loginUserInfo.setText("Stubbed Mode user");
		} else if (Util.getProgramMode() == ProgramMode.JSONRPC) {
			String[] params = {};
			RequestBuilder builder = new RequestBuilder(RequestBuilder.POST,
					URL.encode(Util.getJsonRequest(
							"org.freemedsoftware.core.User.GetName", params)));
			try {
				builder.sendRequest(null, new RequestCallback() {
					public void onError(Request request, Throwable ex) {
						Window.alert(ex.toString());
					}

					public void onResponseReceived(Request request,
							Response response) {
						if (200 == response.getStatusCode()) {
							String userName = (String) JsonUtil.shoehornJson(
									response.getText(), "String");
							loginUserInfo.setText(userName.trim());
							CurrentState.assignDefaultUser(userName);
						}
					}
				});
			} catch (RequestException e) {
				Window.alert(e.getMessage());
			}
		} else {

			// TODO normal mode code goes here
		}
	}

	public Label getStatusBar() {
		return statusBar1;
	}

	public DecoratedTabPanel getTabPanel() {
		return tabPanel;
	}

	public void hide() {
		RootPanel.setVisible(RootPanel.get("rootPanel").getElement(), false);
	}

	public void setFreemedInterface(FreemedInterface i) {
		freemedInterface = i;
		CurrentState.assignFreemedInterface(i);
	}

	public void show() {
		RootPanel.setVisible(RootPanel.get("rootPanel").getElement(), true);
	}

	public void populateDefaultProvider() {
		if (CurrentState.getDefaultProvider().intValue() < 1) {
			if (Util.getProgramMode() == ProgramMode.STUBBED) {
				// Do gornicht.
			} else if (Util.getProgramMode() == ProgramMode.JSONRPC) {
				String[] params = {};
				RequestBuilder builder = new RequestBuilder(
						RequestBuilder.POST,
						URL
								.encode(Util
										.getJsonRequest(
												"org.freemedsoftware.api.UserInterface.GetCurrentProvider",
												params)));
				try {
					builder.sendRequest(null, new RequestCallback() {
						public void onError(Request request, Throwable ex) {
							CurrentState
									.getToaster()
									.addItem(
											"MainScreen",
											_("Could not determine provider information."),
											Toaster.TOASTER_ERROR);
						}

						public void onResponseReceived(Request request,
								Response response) {
							if (200 == response.getStatusCode()) {
								Integer r = (Integer) JsonUtil.shoehornJson(
										JSONParser.parse(response.getText()),
										"Integer");
								if (r != null) {
									JsonUtil
											.debug("MainScreen.populateDefaultProvider: found "
													+ r.toString());
									CurrentState.assignDefaultProvider(r);
								} else {
									JsonUtil
											.debug("MainScreen.populateDefaultProvider: found error");
								}
							} else {
								CurrentState
										.getToaster()
										.addItem(
												"MainScreen",
												_("Could not determine provider information."),
												Toaster.TOASTER_ERROR);
							}
						}
					});
				} catch (RequestException e) {
					Util.showErrorMsg("MainScreen",
							_("Could not determine provider information."));
				}
			} else {
				// TODO: GWT-RPC support for this function
			}
		} else {
			JsonUtil
					.debug("MainScreen.populateDefaultProvider: already assigned");
		}
	}

	public void populateDefaultFacility() {
		if (Util.getProgramMode() == ProgramMode.STUBBED) {
			// Do gornicht.
		} else if (Util.getProgramMode() == ProgramMode.JSONRPC) {
			String[] params = {};
			RequestBuilder builder = new RequestBuilder(
					RequestBuilder.POST,
					URL
							.encode(Util
									.getJsonRequest(
											"org.freemedsoftware.module.FacilityModule.GetDefaultFacility",
											params)));
			try {
				builder.sendRequest(null, new RequestCallback() {
					public void onError(Request request, Throwable ex) {
						Util.showErrorMsg("MainScreen",
								_("Could not determine Facility information."));
					}

					public void onResponseReceived(Request request,
							Response response) {
						if (200 == response.getStatusCode()
								&& response.getText() != null
								&& response.getText() != "null") {
							@SuppressWarnings("unchecked")
							HashMap<String, String> data = (HashMap<String, String>) JsonUtil
									.shoehornJson(JSONParser.parseStrict(response
											.getText()),
											"HashMap<String,String>");
							if (data != null) {
								JsonUtil
										.debug("MainScreen.populateDefaultFacility: found "
												+ data.get("facility"));
								CurrentState.assignDefaultFacility(Integer
										.parseInt(data.get("id")));
								setFacility(data.get("facility"));
							} else {
								JsonUtil
										.debug("MainScreen.populateDefaultProvider: found error");

							}
						} else {
							// populateDefaultFacility();
						}
					}
				});
			} catch (RequestException e) {
				Util.showErrorMsg("MainScreen",
						_("Could not determine Facility information."));
			}
		} else {
			// TODO: GWT-RPC support for this function
		}
	}

	public void emptyLeftNavMenuContainer() {
		JsonUtil.debug("MainScreen.emptyLeftNavMenuContainer: start");
		Iterator<String> itr = leftNavMenuContainer.keySet().iterator();
		while (itr.hasNext()) {
			String key = itr.next();
			MenuIcon menuIcon = leftNavMenuContainer.get(key);
			menuIcon.removeFromParent();
			menuIcon = null;
		}
		leftNavMenuContainer.clear();
		removeWighetFromStack(mainAccPanel);
		removeWighetFromStack(patientAccPanel);
		removeWighetFromStack(documentAccPanel);
		removeWighetFromStack(billingAccPanel);
		removeWighetFromStack(reportingAccPanel);
		removeWighetFromStack(utilitiesAccPanel);
		JsonUtil.debug("MainScreen.emptyLeftNavMenuContainer: ends");
	}

	public void refreshMainScreen() {
		JsonUtil.debug("MainScreen.refreshMainScreen: start");
		setLoginUserInfo();
		notifications.start();
		// emptyLeftNavMenuContainer();
		CurrentState.retrieveUserConfiguration(true, new Command() {
			public void execute() {
				JsonUtil.debug("MainScreen: Set State of dashboard");
				// dashboard.afterStateSet();
				// dashboard.refreshDashBoardWidgets();
				// dashboard.refreshDashBoardWidgets();
				dashboardScreenNew.loadWidgets();
				dashboardScreenNew.reloadDashboard();
				initMainScreen();
			}
		});
		CurrentState.retrieveSystemConfiguration(false);
		dashboardScreenNew.clearView();
		JsonUtil.debug("MainScreen.refreshMainScreen: end");
	}

	public void setFacility(String facility) {
		facilityInfo.setText(facility);
	}
}
