<?php
 // note: patient database functions
 // code: jeff b (jeff@ourexchange.net)
 //       adam b (gdrago23@yahoo.com)
 //       some small stuff by: max k <amk@span.ch>
 // lic : GPL, v2
 
$page_name = "patient.php"; // for help info, later
$record_name = "Patient";   // compatibility with API functions
include_once ("lib/freemed.php");
include_once ("lib/calendar-functions.php");

// Create user object
if (!is_object($this_user)) {
	$this_user = CreateObject('FreeMED.User');
}

if ( ($id>0) AND 
       ($action != "addform") AND ($action != "add") AND
       ($action != "delform") AND ($action != "del")) {
    SetCookie ("current_patient", $id, time()+$_cookie_expire);
    $current_patient = $id;   // patch for first time....
} else { $current_patient = 0; }

//----- Logon/authenticate
freemed_open_db ();

//------HIPAA Logging
$user_to_log=$_SESSION['authdata']['user'];
if((LOGLEVEL<1)||LOG_HIPAA){syslog(LOG_INFO,"patient.php|user $user_to_log accesses");}	
// In order to be compliant I need to be able to log which patient is viewed here
// $id $patient and $current_patient dont seem to work




//---- Push page onto stack
page_push();

switch ($action) {
  case "add": case "addform":
  case "mod": case "modform":
    // addform and modform not used due to "notebook"
   $book = CreateObject('PHP.notebook', array ("action", "id", "been_here"),
     NOTEBOOK_COMMON_BAR|NOTEBOOK_STRETCH, 3);
   $book->set_cancel_name(__("Cancel"));
   $book->set_refresh_name(__("Refresh"));
   switch ($action) {
     case "add": case "addform":
	$book->set_submit_name (__("Add"));
      if ( !$book->been_here() ) {
        // $ins_disp_inactive=false; // TODO! not implemented
      } // end of checking empty been_here
      $action_name = __("Add");
      break; // end internal add

     case "mod": case "modform":
	$book->set_submit_name (__("Modify"));
      if ( !$book->been_here() ) {
      $result = $sql->query("SELECT * FROM patient ".
         "WHERE ( id = '".prepare($id)."' )");

      $r = $sql->fetch_array($result); // dump into array r[]

	// Globalize all fields from the form
	foreach ($r AS $k => $v) {
		global ${$k};
		${$k} = stripslashes($v);
	}

        $ptstate      = strtoupper ($ptstate);

        // resplit email
        if (strlen($ptemail)>3) {
          $ptemail_array = explode ("@", $ptemail);
          $ptemail1      = $ptemail_array[0];
          $ptemail2      = $ptemail_array[1];
        } // end of resplit email
	
        //$ins_disp_inactive=false;
        $been_here = "1"; // set been_here
      } // end of checking empty been_here
      $action_name = __("Modify");
      break; // end internal mod
   } // end of internal switch add/mod

	if (freemed::config_value("folded") == "yes") {
   // ** DISPLAY ADD/MOD ***
   $book->add_page (
     __("Primary Information"),
     array ("ptlname", "ptfname", "ptmname",
            date_vars("ptdob"),
            "ptaddr1", "ptaddr2", "ptcity", "ptstate", "ptzip", "ptcountry",
            "has_insurance"),
		html_form::form_table ( array (
			__("Last Name") =>
				html_form::text_widget("ptlname", 25, 50),
    
			__("First Name") =>
				html_form::text_widget("ptfname", 25, 50),

			__("Middle Name") =>
				html_form::text_widget("ptmname", 25, 50),

			__("Address Line 1") =>
				html_form::text_widget("ptaddr1", 25, 45),

			__("Address Line 2") =>
				html_form::text_widget("ptaddr2", 25, 45),

			__("City").", ".__("State").", ".__("Zip") =>
				html_form::text_widget("ptcity", 10, 45)."\n".
				html_form::state_pulldown("ptstate").
				html_form::text_widget("ptzip", 10),

			__("Date of Birth") =>
				date_entry("ptdob")


		) )
     );

     $book->add_page(
       __("Contact"),
       array (
         "ptaddr1", "ptaddr2", "ptcity", "ptstate", "ptzip",
	 "ptcountry",
          // phone_vars("pthphone"),
         "pthphone",
         "pthphone_1", "pthphone_2", "pthphone_3", "pthphone_4", "pthphone_5",
          // phone_vars("ptwphone"),
         "ptwphone",
         "ptwphone_1", "ptwphone_2", "ptwphone_3", "ptwphone_4", "ptwphone_5",
	  // phone_vars("ptfax")
         "ptfax",
         "ptfax_1", "ptfax_2", "ptfax_3", "ptfax_4", "ptfax_5",
          // email address portions
         "ptemail1", "ptemail2"
         ),
		html_form::form_table ( array (

			__("Country") =>
				html_form::country_pulldown("ptcountry"),

			__("Home Phone") =>
				fm_phone_entry ("pthphone"),

			__("Work Phone") =>
				fm_phone_entry ("ptwphone"),
    
			__("Fax Number") =>
				fm_phone_entry ("ptfax"),
  
			__("Email Address") =>
				"<INPUT TYPE=TEXT NAME=\"ptemail1\" SIZE=20 MAXLENGTH=40 ".
				"VALUE=\"".prepare($ptemail1)."\"> <B>@</B>\n".
				"<INPUT TYPE=TEXT NAME=\"ptemail2\" SIZE=20 MAXLENGTH=40 ".
				"VALUE=\"".prepare($ptemail2)."\">"

		) )
     );

   	 $ptstatus_r = $sql->query("SELECT ptstatus,ptstatusdescrip,id
                            FROM ptstatus
                            ORDER BY ptstatus");
     $book->add_page(
       __("Personal"),
       array (
         "ptsex", "ptmarital", "ptssn", "ptid",
	 "ptdmv", "ptbilltype", "ptbudg", "ptempl"
	 ),
	html_form::form_table ( array (

		__("Gender") =>
			html_form::select_widget("ptsex",
				array (
					 __("Female")        => "f",
					 __("Male")          => "m",
					 __("Transgendered") => "t"
				)
			),

		__("Marital Status") =>
			html_form::select_widget("ptmarital",
				array (
					__("Single")    => "single",
					__("Married")   => "married",
					__("Divorced")  => "divorced",
					__("Separated") => "separated",
					__("Widowed")   => "widowed",
				)
			),
	
		__("Employment Status") =>
			html_form::select_widget("ptempl",
				array (
					__("Yes")    => "y",
					__("No")     => "n",
					__("Part Time") => "p",
					__("Self")     => "s",
					__("Retired")  => "r",
					__("Military") => "m",
					__("Unknown")  => "u"
				)
			),
		__("Patient Status") => 
  			freemed_display_selectbox ($ptstatus_r, "#ptstatus#, #ptstatusdescrip", "ptstatus"),

		__("Social Security Number") =>
			html_form::text_widget("ptssn", 9),

		__("Internal Practice ID #") =>
			html_form::text_widget("ptid", 10),
    
		__("Driver's License (No State)") =>
			html_form::text_widget("ptdmv", 9),
       
		__("Type of Billing") =>
			html_form::select_widget("ptbilltype",
				array (
					__("Monthly Billing On Account") => "mon",
					__("Statement Billing")          => "sta",
					__("Charge Card Billing")        => "chg",
					__("NONE SELECTED")              => ""
				)
			),

		__("Monthly Budget Amount") =>
			"<INPUT TYPE=TEXT NAME=ptbudg SIZE=10 MAXLENGTH=20 ".
			"VALUE=\"".prepare($ptbudg)."\">"
		) )

	);


   $ref_phys_r = $sql->query("SELECT phylname,phyfname,id
                            FROM physician WHERE phyref='yes' 
                            ORDER BY phylname,phyfname");
   $int_phys_r = $sql->query("SELECT phylname,phyfname,id
                            FROM physician WHERE phyref='no' 
                            ORDER BY phylname,phyfname");
   $all_phys_r = $sql->query("SELECT phylname,phyfname,id
                            FROM physician
			    ORDER BY phylname,phyfname");

   if (!isset($num_other_docs)) { // first time through
     $num_other_docs=0;
     for ($i=1;$i<=4;$i++) // for ptphy[1..4]
       if (${"ptphy$i"}>0) 
         $num_other_docs++; // some days, i'm so clever it hurts.
   } // is !isset num_other_docs

   $book->add_page(
     __("Physician"),
     array ("ptdoc", "ptphy1", "ptphy2", "ptphy3", "ptphy4", "ptpcp",
            "ptrefdoc", "num_other_docs"),
     "
    <TABLE CELLSPACING=0 CELLPADDING=2 BORDER=0>

    <TR><TD ALIGN=RIGHT>
    ".__("In House Doctor")." :
    </TD><TD ALIGN=LEFT>
  ".freemed_display_selectbox ($int_phys_r, "#phylname#, #phyfname#", "ptdoc")."
    </TD></TR>

    <TR><TD ALIGN=RIGHT>
    ".__("Referring Doctor")." :
    </TD><TD ALIGN=LEFT>
  ".freemed_display_selectbox ($ref_phys_r, "#phylname#, #phyfname#", "ptrefdoc")."
    </TD></TR>

    <TR><TD ALIGN=RIGHT>
    ".__("Primary Care Physician")." :
    </TD><TD ALIGN=LEFT>
  ".freemed_display_selectbox ($all_phys_r, "#phylname#, #phyfname#", "ptpcp")."
    </TD></TR>

    ".(($num_other_docs>0) ? "
    <TR><TD ALIGN=RIGHT>
    ".__("Other Physician")." 1 :
    </TD><TD ALIGN=LEFT>
  ".freemed_display_selectbox ($all_phys_r, "#phylname#, #phyfname#", "ptphy1")."
    </TD></TR>
    " : "").

    (($num_other_docs>1) ? "
    <TR><TD ALIGN=RIGHT>
    ".__("Other Physician")." 2 :
    </TD><TD ALIGN=LEFT>
  ".freemed_display_selectbox ($all_phys_r, "#phylname#, #phyfname#", "ptphy2")."
    </TD></TR>
    " : "").

    (($num_other_docs>2) ? "
    <TR><TD ALIGN=RIGHT>
    ".__("Other Physician")." 3 :
    </TD><TD ALIGN=LEFT>
  ".freemed_display_selectbox ($all_phys_r, "#phylname#, #phyfname#", "ptphy3")."
    </TD></TR>
    " : "").

    (($num_other_docs>3) ? "
    <TR><TD ALIGN=RIGHT>
    ".__("Other Physician 4")." :
    </TD><TD ALIGN=LEFT>
  ".freemed_display_selectbox ($all_phys_r, "#phylname#, #phyfname#", "ptphy4")."
    </TD></TR>
    " : "").

    "<TR><TD ALIGN=RIGHT>
    ".__("Number of Other Physicians")." :
    </TD><TD ALIGN=LEFT>
      ".html_form::number_pulldown("num_other_docs", 0, 4)."
      ".$book->generate_refresh()."
    </TD></TR>

    </TABLE>
     ");    

	$book->add_page(
		__("Medical"),
		array("ptblood"),
		html_form::form_table(array(
			__("Blood Type") =>
			html_form::select_widget(
				"ptblood",
				array(
					"O",
					"O+",
					"O-",
					"A",
					"A+",
					"A-",
					"B",
					"B+",
					"B-",
					"AB",
					"AB+",
					"AB-"
				)
			)
		))
	);

   $book->add_page(
     __("Notes"),
     array("ptnextofkin"),
     html_form::form_table(array(
       "" => "<div ALIGN=\"CENTER\">".
	html_form::text_area("ptnextofkin", "VIRTUAL", 10, 40).
	"</div>"
     ))
   );

	} else { // checking for folded


   	 $ptstatus_r = $sql->query("SELECT ptstatus,ptstatusdescrip,id
                            FROM ptstatus
                            ORDER BY ptstatus");
   $ref_phys_r = $sql->query("SELECT phylname,phyfname,id
                            FROM physician WHERE phyref='yes' 
                            ORDER BY phylname,phyfname");
   $int_phys_r = $sql->query("SELECT phylname,phyfname,id
                            FROM physician WHERE phyref='no' 
                            ORDER BY phylname,phyfname");
   $all_phys_r = $sql->query("SELECT phylname,phyfname,id
                            FROM physician
			    ORDER BY phylname,phyfname");

   if (!isset($num_other_docs)) { // first time through
     $num_other_docs=0;
     for ($i=1;$i<=4;$i++) // for ptphy[1..4]
       if (${"ptphy$i"}>0) 
         $num_other_docs++; // some days, i'm so clever it hurts.
   } // is !isset num_other_docs

   $book->add_page (
     __("Patient"),
     array ("ptlname", "ptfname", "ptmname",
            date_vars("ptdob"),
            "ptaddr1", "ptaddr2", "ptcity", "ptstate", "ptzip", "ptcountry",
            "has_insurance",
         "ptaddr1", "ptaddr2", "ptcity", "ptstate", "ptzip",
	 "ptcountry",
          // phone_vars("pthphone"),
         "pthphone",
         "pthphone_1", "pthphone_2", "pthphone_3", "pthphone_4", "pthphone_5",
          // phone_vars("ptwphone"),
         "ptwphone",
         "ptwphone_1", "ptwphone_2", "ptwphone_3", "ptwphone_4", "ptwphone_5",
	  // phone_vars("ptfax")
         "ptfax",
         "ptfax_1", "ptfax_2", "ptfax_3", "ptfax_4", "ptfax_5",
          // email address portions
         "ptemail1", "ptemail2",
         "ptsex", "ptmarital", "ptssn", "ptid",
	 "ptdmv", "ptbilltype", "ptbudg", "ptempl",
	"ptdoc", "ptphy1", "ptphy2", "ptphy3", "ptphy4", "ptpcp",
            "ptrefdoc", "num_other_docs",
	"ptblood",
     	"ptnextofkin"
         ),

		html_form::form_table ( array (
			__("Last Name") =>
				html_form::text_widget("ptlname", 25, 50),
    
			__("First Name") =>
				html_form::text_widget("ptfname", 25, 50),

			__("Middle Name") =>
				html_form::text_widget("ptmname", 25, 50),

			__("Address Line 1") =>
				html_form::text_widget("ptaddr1", 25, 45),

			__("Address Line 2") =>
				html_form::text_widget("ptaddr2", 25, 45),

			__("City").", ".__("State").", ".__("Zip") =>
				html_form::text_widget("ptcity", 10, 45)."\n".
				html_form::state_pulldown("ptstate").
				html_form::text_widget("ptzip", 10),

			__("Date of Birth") =>
				date_entry("ptdob"),

			__("Country") =>
				html_form::country_pulldown("ptcountry"),

			__("Home Phone") =>
				fm_phone_entry ("pthphone"),

			__("Work Phone") =>
				fm_phone_entry ("ptwphone"),
    
			__("Fax Number") =>
				fm_phone_entry ("ptfax"),
  
			__("Email Address") =>
				"<INPUT TYPE=TEXT NAME=\"ptemail1\" SIZE=20 MAXLENGTH=40 ".
				"VALUE=\"".prepare($ptemail1)."\"> <B>@</B>\n".
				"<INPUT TYPE=TEXT NAME=\"ptemail2\" SIZE=20 MAXLENGTH=40 ".
				"VALUE=\"".prepare($ptemail2)."\">",

		__("Gender") =>
			html_form::select_widget("ptsex",
				array (
					 __("Female")        => "f",
					 __("Male")          => "m",
					 __("Transgendered") => "t"
				)
			),

		__("Marital Status") =>
			html_form::select_widget("ptmarital",
				array (
					__("Single")    => "single",
					__("Married")   => "married",
					__("Divorced")  => "divorced",
					__("Separated") => "separated",
					__("Widowed")   => "widowed"
				)
			),
	
		__("Employment Status") =>
			html_form::select_widget("ptempl",
				array (
					__("Yes")    => "y",
					__("No")     => "n",
					"Part Time" => "p",
					"Self"      => "s",
					"Retired"   => "r",
					"Military"  => "m",
					"Unknown"   => "u"
				)
			),
		__("Patient Status") => 
  			freemed_display_selectbox ($ptstatus_r, "#ptstatus#, #ptstatusdescrip", "ptstatus"),

		__("Social Security Number") =>
			html_form::text_widget("ptssn", 9),

		__("Internal Practice ID #") =>
			html_form::text_widget("ptid", 10),
    
		__("Driver's License (No State)") =>
			html_form::text_widget("ptdmv", 9),
       
		__("Type of Billing") =>
			html_form::select_widget("ptbilltype",
				array (
					__("Monthly Billing On Account") => "mon",
					__("Statement Billing")          => "sta",
					__("Charge Card Billing")        => "chg",
					__("NONE SELECTED")              => ""
				)
			),

		__("Monthly Budget Amount") =>
			"<INPUT TYPE=TEXT NAME=ptbudg SIZE=10 MAXLENGTH=20 ".
			"VALUE=\"".prepare($ptbudg)."\">",
		 
	__("In House Doctor") =>
	freemed_display_selectbox ($int_phys_r, "#phylname#, #phyfname#", "ptdoc"),

	__("Referring Doctor") =>
	freemed_display_selectbox ($ref_phys_r, "#phylname#, #phyfname#", "ptrefdoc"),

	__("Primary Care Physician") =>
	freemed_display_selectbox ($all_phys_r, "#phylname#, #phyfname#", "ptpcp"),

	(($num_other_docs>0) ? __("Other Physician")." 1" : "" ) =>
	freemed_display_selectbox ($all_phys_r, "#phylname#, #phyfname#", "ptphy1"),

	(($num_other_docs>1) ? __("Other Physician")." 2" : "" ) =>
	freemed_display_selectbox ($all_phys_r, "#phylname#, #phyfname#", "ptphy2"),

	(($num_other_docs>2) ? __("Other Physician")." 3" : "" ) =>
	freemed_display_selectbox ($all_phys_r, "#phylname#, #phyfname#", "ptphy3"),

	(($num_other_docs>3) ? __("Other Physician")." 4" : "" ) =>
	freemed_display_selectbox ($all_phys_r, "#phylname#, #phyfname#", "ptphy4"),

	__("Number of Other Physicians") =>
	html_form::number_pulldown("num_other_docs", 0, 4).
        $book->generate_refresh(),

			__("Blood Type") =>
			html_form::select_widget(
				"ptblood",
				array(
					"O",
					"O+",
					"O-",
					"A",
					"A+",
					"A-",
					"B",
					"B+",
					"B-",
					"AB",
					"AB+",
					"AB-"
				)
			),
       __("Next of Kin") =>
	html_form::text_area("ptnextofkin", "VIRTUAL", 10, 40)
     ))
   );

	} // end checking for folded

   // show notebook
   $page_title = __("Patient")." ".$action_name;
   if ( ($action=="modform") or ($action=="mod")) {
     $this_patient = CreateObject('FreeMED.Patient', $id);
     $display_buffer .= freemed::patient_box ($this_patient);
   }
	// Handle cancel action
	if ($book->is_cancelled()) {
		switch($action) {
			case "add": case "addform":
			global $patient;
			Header("Location: patient.php");
			die(""); break;

			case "mod": case "modform":
			global $patient;
			Header("Location: manage.php?id=".urlencode($id));
			die(""); break;
		}
	}

   if (!( $book->is_done() )) {
     $display_buffer .= "<div ALIGN=\"CENTER\">\n".$book->display()."</div>\n";
   } else { // if it is done
     switch ($action) {
       case "add": case "addform":
         $ptdtadd = $cur_date; // current date of add...
         $ptdtmod = $cur_date; // current date for mod as well

         // next of kin prepare blob field
         $ptnextofkin = addslashes ($ptnextofkin);

         // assemble phone numbers
         $pthphone   = fm_phone_assemble ("pthphone");
         $ptwphone   = fm_phone_assemble ("ptwphone");
         $ptfax      = fm_phone_assemble ("ptfax");

         // assemble dates
         $ptdob      = fm_date_assemble("ptdob");

         // knock state to upper case
         $ptstate  = strtoupper ($ptstate); 

         // assemble email
         if ((strlen($ptemail1)>0) AND (strlen($ptemail2)>3))
           $ptemail = $ptemail1 . "@" . $ptemail2;
       
         // collapse the TEXT variables...
	 //reset($t_vars);while ($i=next($t_vars)) 
	 //                 $$i = fm_join_from_array($$i);

	 $query = $sql->insert_query (
           "patient",
           array (
             "ptdtadd" => date("Y-m-d"),
	     "ptdob" => fm_date_assemble ("ptdob"),
             "ptbal",
             "ptbalfwd",
             "ptunapp",
             "ptrefdoc",
             "ptpcp",
             "ptphy1",
             "ptphy2",
             "ptphy3",
             "ptphy4",
             "ptbilltype",
             "ptbudg",
             "ptdoc",
             "ptlname",
             "ptfname",
             "ptmname",
             "ptaddr1",
             "ptaddr2",
             "ptcity",
             "ptstate" => strtoupper ($ptstate),
             "ptzip",
             "ptcountry",
             "pthphone"  => fm_phone_assemble ("pthphone"),
             "ptwphone"  => fm_phone_assemble ("ptwphone"),
             "ptfax" => fm_phone_assemble ("ptfax"),
             "ptemail",
             "ptsex",
             "ptssn",
             "ptdmv",
             "ptdtlpay",
             "ptamtlpay" => $ptpaytype,
             "ptstatus",
             "ptytdchg",
             "ptar",
             "ptextinf",
             "ptdisc",
             "ptdiag1",
             "ptdiag2",
             "ptdiag3",
             "ptdiag4",
             "ptid",
             "pthistbal",
             "ptmarital",
             "ptempl",
             "ptemp1",
             "ptemp2",
             "ptguar",
             "ptguarstart",
             "ptguarend",
             "ptrelguar",
             "ptins",
             "ptinsno",
             "ptinsgrp",
             "ptinsstart",
             "ptinsend",
             "ptnextofkin",
             "ptblood",
             "pttimestamp" => '',
             "ptemriversion" => '1',
             "iso" => $__ISO_SET__
            ) );
	 break; // end add

       case "mod": case "modform":
         // collapse the TEXT variables...
	 //reset($t_vars);while ($i=next($t_vars)) 
	 //                 if (is_array($$i)) $$i = implode(':', $$i);
         //$ptins{start,end} already fm_date_assemble'd
	 // reassemble email
	 if ((strlen($ptemail1)>1) AND (strlen($ptemail2)>3))
	   $ptemail = $ptemail1 . "@" . $ptemail2;
	 $query = $sql->update_query (
           "patient",
           array (
             "ptdtmod" => date("Y-m-d"),
	     "ptdob" => fm_date_assemble ("ptdob"),
             "ptbal",
             "ptbalfwd",
             "ptunapp",
             "ptrefdoc",
             "ptpcp",
             "ptphy1",
             "ptphy2",
             "ptphy3",
             "ptphy4",
             "ptbilltype",
             "ptbudg",
             "ptdoc",
             "ptlname",
             "ptfname",
             "ptmname",
             "ptaddr1",
             "ptaddr2",
             "ptcity",
             "ptstate" => strtoupper ($ptstate),
             "ptzip",
             "ptcountry",
             "pthphone"  => fm_phone_assemble ("pthphone"),
             "ptwphone"  => fm_phone_assemble ("ptwphone"),
             "ptfax" => fm_phone_assemble ("ptfax"),
             "ptemail",
             "ptsex",
             "ptssn",
             "ptdmv",
             "ptdtlpay",
             "ptamtlpay" => $ptpaytype,
             "ptstatus",
             "ptytdchg",
             "ptar",
             "ptextinf",
             "ptdisc",
             "ptdiag1",
             "ptdiag2",
             "ptdiag3",
             "ptdiag4",
             "ptid",
             "pthistbal",
             "ptmarital",
             "ptempl",
             "ptemp1",
             "ptemp2",
             "ptguar",
             "ptguarstart",
             "ptguarend",
             "ptrelguar",
             "ptins",
             "ptinsno",
             "ptinsgrp",
             "ptinsstart",
             "ptinsend",
             "ptnextofkin",
             "ptblood",
             "pttimestamp" => '',
             "iso"
            ), array ( "id" => $id )
         );
         break; // end mod
     } // end switch for action (done .. actual action)
     $display_buffer .= "
      <div ALIGN=\"CENTER\"><b>".( (($action=="mod") OR ($action=="modform")) ?
             __("Modifying") : __("Adding") )." ...</b> ";
     $result = $sql->query($query);
     if ($result) $display_buffer .= __("Done");
     else $display_buffer .= __("Error");
     $display_buffer .= "<br/>\n";
	 if ( ($result) AND ($action=="addform") AND (empty($ptid)) )
	 {
		$display_buffer .= "<b>".__("Adding Patient ID")." ...</b> ";
		$pid = $sql->last_record($result);
		$patid = PATID_PREFIX.$pid;
		$result = $sql->query("UPDATE patient SET ptid='".addslashes($patid)."' ".
			"WHERE id='".addslashes($pid)."'");
     	if ($result) $display_buffer .= __("Done");
     	else $display_buffer .= __("Error");
		$display_buffer .= "<br/>\n";
		
	 }

	// Set automatic page refresh to management screen
	$refresh = "manage.php?id=".( $action=="addform" ? $pid : $id );

     $display_buffer .= "
      <p/>
      <a HREF=\"manage.php?id=".( $action=="addform" ? $pid : $id )."\">
      ".__("Manage This Patient")."
      </a>
      
      </div>
     ";
   } // end checking if done

   break; // end action add/mod

  case "delete":
  case "del":
    $page_title = __("Deleting")." "._($record_name);
    $display_buffer .= "<div ALIGN=\"CENTER\">
     <p/>".__("Deleting")." ... ";
    $query = "DELETE FROM patient WHERE id='".addslashes($id)."'";
    $result = $sql->query ($query);
    if ($result) { $display_buffer .= __("done")."."; }
     else        { $display_buffer .= __("ERROR");    }
    $display_buffer .= "
     </div>
    ";
  break; // end action delete

  case "find":
    switch ($criteria) {
      case "letter":
        $query = "SELECT ptlname,ptfname,ptdob,ptid,id FROM patient ".
         "WHERE (ptlname LIKE '".addslashes($f1)."%') ".
	 freemed::itemlist_conditions(false).
         "ORDER BY ptlname, ptfname, ptdob";
        $_crit = __("Last Names")." (".prepare($f1).")";
        break;
      case "contains":
        $query = "SELECT ptlname,ptfname,ptdob,ptid,id FROM patient ".
         "WHERE (".addslashes($f1)." LIKE '%".addslashes($f2)."%') ".
	 freemed::itemlist_conditions(false).
         "ORDER BY ptlname, ptfname, ptdob";
        $_crit = __("Searching for")." \"".prepare($f2)."\"";
        break;
      case "soundex":
        $query = "SELECT ptlname,ptfname,ptdob,ptid,id FROM patient ".
         "WHERE (soundex(".addslashes($f1).") = soundex('".addslashes($f2)."')) ".
	 freemed::itemlist_conditions(false).
         "ORDER BY ptlname, ptfname, ptdob";
        $_crit = "Sounds Like \"".prepare($f2)."\"";
        break;
      case "all":
        $query = "SELECT ptlname,ptfname,ptdob,ptid,id FROM patient ".
	 freemed::itemlist_conditions().
         "ORDER BY ptlname, ptfname, ptdob";
        $_crit = "\"".__("All Patients")."\"";
        break;
      case "dependants":
        $query = "SELECT ptlname,ptfname,ptdob,ptid,id FROM patient ".
         "WHERE (ptdep = '".addslashes($f1)."') ".
	 freemed::itemlist_conditions(false).
         "ORDER BY ptlname, ptfname, ptdob";
        $_crit = __("Dependents");
        break;
      case "guarantor":
        $query = "SELECT ptlname,ptfname,ptdob,ptid,id FROM patient ".
         "WHERE (id = '".addslashes($f1)."') ".
	 freemed::itemlist_conditions(false).
         "ORDER BY ptlname, ptfname, ptdob";
        $_crit = __("Guarantor");
        break;
      default:
        $_crit = "";
        break;
    } // end criteria search

    $result = $sql->query($query); 

	// Check to see if there's only one result, and jump to them
	// if it's found
	if ($result and ($sql->num_rows($result)==1)) {
		// Go to beginning
		$sql->data_seek($result, 0);
		// Grab the data
		$_r = $sql->fetch_array($result);
		// Form refresh string to pass to template
		$refresh = "manage.php?id=".urlencode($_r['id']);
		// Reset data so that the display works (in case of no refresh)
		$sql->data_seek($result, 0);
	} // end checking for single patient jump

      $page_title = __("Patients Meeting Criteria")." ".$_crit;

      if (strlen($_ref)<5) {
        $_ref="main.php";
      } // if no ref, then return to home page...

      $display_buffer .= freemed_display_itemlist(
        $result,
	$page_name,
	array (
	  __("Last Name") =>     "ptlname",
	  __("First Name") =>    "ptfname",
	  __("Date of Birth") => "ptdob",
	  __("Practice ID") =>   "ptid"
	),
	array ("","",""),
	"", "", "",
	ITEMLIST_MOD|ITEMLIST_VIEW
      );

      $display_buffer .= "<p/>\n";
  break; // end action find
 
  case "display":
  case "view":
    // KludgE AlerTx0r!
    header("Location:".ereg_replace("patient.php",
           "manage.php", basename($REQUEST_URI)));
  break;

  default: // default action

    // Set page title
    $page_title = __("Patients");

    // Push onto stack
    page_push();
 
    if ($_COOKIE['current_patient'] > 0) {
      $this_patient = CreateObject('FreeMED.Patient', $_COOKIE['current_patient']);
      $display_buffer .= freemed::patient_box ($this_patient);
    }
 
    if (freemed::user_flag(USER_DATABASE)) {
      $display_buffer .= "
        <table WIDTH=\"100%\" CLASS=\"reverse\" BORDER=\"0\" CELLSPACING=\"0\"
         CELLPADDING=\"0\" VALIGN=\"TOP\" ALIGN=\"CENTER\"><tr><td>
      ";
      $result = $sql->query ("SELECT COUNT(*) FROM patient");
      if ($result) {
        $_res   = $sql->fetch_array ($result);
        $_total = $_res[0];               // total number in db
  
        if ($_total>1)
          $display_buffer .= "
            <div ALIGN=\"CENTER\">
             <b><i>$_total ".__("Patient(s) In System")."</i></b>
            </div>
          ";
        elseif ($_total==0)
          $display_buffer .= "
            <div ALIGN=\"CENTER\">
             <b><i>".__("No Patients In System")."</i></b>
            </div>
          ";
        elseif ($_total==1)
          $display_buffer .= "
            <div ALIGN=\"CENTER\">
            <b><i>".__("One Patient In System")."</i></b>
            </div>
          ";
      } else {
        $display_buffer .= "
          <div ALIGN=\"CENTER\">
           <b><i>".__("No Patients In System")."</i></b>
          </div>
        ";
      } // if there are none...
      $display_buffer .= "
        </td></tr></table>
      "; // end table statement for bar
    }

    $display_buffer .= "
      <br/>
      <div ALIGN=\"CENTER\">
       <b>".__("Patients By Name")."</b>
      <br/>
      <a HREF=\"$page_name?action=find&criteria=letter&f1=A\">A</a>
      <a HREF=\"$page_name?action=find&criteria=letter&f1=B\">B</a>
      <a HREF=\"$page_name?action=find&criteria=letter&f1=C\">C</a>
      <a HREF=\"$page_name?action=find&criteria=letter&f1=D\">D</a>
      <a HREF=\"$page_name?action=find&criteria=letter&f1=E\">E</a>
  
      <a HREF=\"$page_name?action=find&criteria=letter&f1=F\">F</a>
      <a HREF=\"$page_name?action=find&criteria=letter&f1=G\">G</a>
      <a HREF=\"$page_name?action=find&criteria=letter&f1=H\">H</a>
      <a HREF=\"$page_name?action=find&criteria=letter&f1=I\">I</a>
      <a HREF=\"$page_name?action=find&criteria=letter&f1=J\">J</a>
  
      <a HREF=\"$page_name?action=find&criteria=letter&f1=K\">K</a>
      <a HREF=\"$page_name?action=find&criteria=letter&f1=L\">L</a>
      <a HREF=\"$page_name?action=find&criteria=letter&f1=M\">M</a>
      <br/>
      <a HREF=\"$page_name?action=find&criteria=letter&f1=N\">N</a>
      <a HREF=\"$page_name?action=find&criteria=letter&f1=O\">O</a>
      <a HREF=\"$page_name?action=find&criteria=letter&f1=P\">P</a>
      <a HREF=\"$page_name?action=find&criteria=letter&f1=Q\">Q</a>
      <a HREF=\"$page_name?action=find&criteria=letter&f1=R\">R</a>
  
      <a HREF=\"$page_name?action=find&criteria=letter&f1=S\">S</a>
      <a HREF=\"$page_name?action=find&criteria=letter&f1=T\">T</a>
      <a HREF=\"$page_name?action=find&criteria=letter&f1=U\">U</a>
      <a HREF=\"$page_name?action=find&criteria=letter&f1=V\">V</a>
      <a HREF=\"$page_name?action=find&criteria=letter&f1=W\">W</a>
  
      <a HREF=\"$page_name?action=find&criteria=letter&f1=X\">X</a>
      <a HREF=\"$page_name?action=find&criteria=letter&f1=Y\">Y</a>
      <a HREF=\"$page_name?action=find&criteria=letter&f1=Z\">Z</a>

      <p/>

      <form ACTION=\"$page_name\" METHOD=\"POST\">
       <b>".__("Patients Field Search")."</b>
      <br/>
      <input TYPE=\"HIDDEN\" NAME=\"action\" VALUE=\"find\"/>
      <input TYPE=\"HIDDEN\" NAME=\"criteria\" VALUE=\"contains\"/>
      ".html_form::select_widget(
        'f1',
	array(
          __("Last Name") => 'ptlname',
	  __("First Name") => 'ptfname',
	  __("Date of Birth") => 'ptdob',
	  __("Internal Practice ID") => 'ptid',
	  __("City") => 'ptcity',
	  __("State") => 'ptstate',
	  __("Zip") => 'ptzip',
	  __("Home Phone") => 'pthphone',
	  __("Work Phone") => 'ptwphone',
	  __("Email Address") => 'ptemail',
	  __("Social Security Number") => 'ptssn',
	  __("Driver's License") => 'ptdmv'
	)
      )."	
      <i><small>".__("contains")."</small></i>
      ".html_form::text_widget('f2', 15, 30)."
      <input class=\"button\" TYPE=\"SUBMIT\" VALUE=\"".__("Search")."\">
      </form>
      <p/>

      <form ACTION=\"$page_name\" METHOD=\"POST\">
      <input TYPE=\"HIDDEN\" NAME=\"action\" VALUE=\"find\"/>
      <input TYPE=\"HIDDEN\" NAME=\"criteria\" VALUE=\"soundex\"/>
      <b>".__("Soundalike Search")."</b><br/>
      ".html_form::select_widget(
        "f1",
	array(
          __("Last Name") => 'ptlname',
	  __("First Name") => 'ptfname'
	)
      )."
        <i><small>".__("sounds like")."</small></i>
      ".html_form::text_widget('f2', 15, 30)."
      <input class=\"button\" TYPE=\"SUBMIT\" VALUE=\"".__("Search")."\">
      </form>
      <p/>
      </div>

      ".template::link_bar(array(
      		__("Show All Patients") =>
      		"$page_name?action=find&criteria=all&f1=",

       		__("Add Patient") =>
      		"$page_name?action=addform",

      		__("Call In Menu") =>
      		"call-in.php",

		__("Return to Main Menu") =>
		"main.php"
      ))."

      <p/> 
    ";

    break; // end default action
} // end action

//----- Display the template
template_display();

?>


