<?php
 // $Id$
 // $Author$

$page_name = basename($_SERVER["PHP_SELF"]);
include_once ("lib/freemed.php");

//----- Login/authenticate
freemed::connect ();

//----- Set page title
$page_title = __("Preferences");

//----- Add page to history
page_push();

//----- Create user object
if (!is_object($this_user)) $this_user = CreateObject('FreeMED.User');

// Check for cancel
if ($submit==__("Cancel")) {
	unset($action);
}

switch ($action) {
	case "passwordform":
	$display_buffer .= "
	<form action=\"".$page_name."\" method=\"POST\">
	<input type=\"HIDDEN\" name=\"action\" value=\"password\">
	<div align=\"center\">
	".html_form::form_table(array(
		__("Current Password") =>
		html_form::password_widget("current_password"),

		__("New Password") =>
		html_form::password_widget("new_password1"),

		__("New Password Again") =>
		html_form::password_widget("new_password2"),
	))."
	</div>
	<div align=\"center\">
	<input class=\"button\" type=\"submit\" name=\"submit\" value=\"".__("Update")."\"/>
	<input class=\"button\" type=\"submit\" name=\"submit\" value=\"".__("Cancel")."\"/>
	</div>
	</form>
	";
	break; // end passwordform

	case "password":

//	$display_buffer = $_SESSION['authdata']['user'];
	$uid=$_SESSION['authdata']['user'];

	$result = $sql->query ("SELECT * FROM user ".
		"WHERE id = '".addslashes($uid)."'");
	
		$r = $sql->fetch_array ($result);

			$current_md5=md5($current_password);

			$db_pass=$r['userpassword'];	


		

		if ($current_md5 == $r['userpassword']) 	
		{
			if($new_password1==$new_password2)
			{
				$display_buffer=$display_buffer."This worked";
				$this_user = CreateObject('FreeMED.User');
				$this_user->setPassword($new_password1,$uid);
				
// change the password
			}
			else
			{
				// print error passwords dont match
				$display_buffer= "The new passwords you entered dont match<BR>";
			}
			

		}
		else
		{
//------HIPAA Logging
$user_to_log=$_SESSION['authdata']['user'];
if((LOGLEVEL<1)||LOG_HIPAA){syslog(LOG_INFO,"preferences.php|user $user_to_log attempt to change password failed.");}	


			$display_buffer = "The password you entered was incorrect";
		}

	$refresh = $page_name;
	template_display();
	break;

	case "templateform":
	// Include proper template file
	if (file_exists("./lib/template/".$template."/id.php")) {
		include_once ("./lib/template/".$template."/id.php");
	} else {
		include_once ("./lib/template/default/id.php");
	}
	
	// Form header
	$display_buffer .= "
		<form action=\"preferences.php\" method=\"post\">
		<input type=\"hidden\" name=\"action\" value=\"template\"/>
	";
	// Loop through template options
	foreach ($TEMPLATE_OPTIONS AS $k => $v) {
		// Handle selects
		if (is_array($v['options'])) {
			$form_parts[$v['name']] = html_form::select_widget(
				$v['var'],
				$v['options']
			);
		}
	}
	// Display form_parts
	$display_buffer .= html_form::form_table($form_parts)."\n";
	$display_buffer .= "
		<div align=\"center\">
		<input type=\"submit\" name=\"submit\" class=\"button\" value=\"".
			__("Set Options")."\"/>
		<input type=\"submit\" name=\"submit\" class=\"button\" value=\"".
			__("Cancel")."\"/>
		</div>
		</form>
	";
	break; // end templateform


	case "template":
	// Include proper template file
	if (file_exists("./lib/template/".$template."/id.php")) {
		include_once ("./lib/template/".$template."/id.php");
	} else {
		include_once ("./lib/template/default/id.php");
	}
	
	// Loop through variables, and set them properly
	foreach ($TEMPLATE_OPTIONS AS $k => $v) {
		if (isset(${$v['var']})) {
			// Keep cookie for a year (find better way to do this)
			SetCookie($v['var'], ${$v['var']}, time()+(60*60*24*365));
		}
	}

	// And use header to move back to action=(nothing)
	$refresh = "preferences.php";
	break; // end template


	default: // display menu
	$display_buffer .= "
	<div align=\"center\">
	<a href=\"preferences.php?action=passwordform\"
	>".__("Change Password")."</a>
	</div>

	<div align=\"center\">
	<a href=\"preferences.php?action=templateform\"
	>".__("Change Template Options")."</a>
	</div>

	<p/>

	<div align=\"center\">
	<a class=\"button\" href=\"main.php\">".__("Return to Main Menu")."</a>
	</div>
	";
	break;
} // end action

//----- Display template
template_display();

?>
