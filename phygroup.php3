<?php
  // file: phygroup.php3
  // note: physician groups, used for booking? and user levels
  // code: [used the template because of pure sloth]
  //       jeff b (jeff@univrel.pr.uconn.edu) -- template
  //       adam b (gdrago23@yahoo.com) -- redesign and update
  // lic : GPL
  // 
  # please note that you _can_ remove the comments down below,
  # but everything above here should remain untouched. please
  # do _not_ remove my name or address from this file, since I
  # have worked very hard on it. the license must also always
  # remain GPL.                                     -- jeff b

    // *** local variables section ***
    // complete these to reflect the data for this
    // module.

  $page_name="phygroup.php3";         // for help info, later
  $db_name  ="phygroup";              // get this from jeff
  $record_name="Physician Group";     // such as Room for Rooms module
                                      // or "CPT Modifiers" for cptmod
  $order_field="phygroupname";        // what field the records are
                                      // sorted by... multiples can
                                      // be used with commas
                                      // ("value_a, value_b")

    // *** includes section ***

  include ("global.var.inc");         // load global variables
  include ("freemed-functions.inc");  // API functions

    // *** setting _ref cookie ***
    // if you are going to be "chaining" out from this
    // function and want users to be able to return to
    // it, uncomment this and it will set the cookie to
    // return people using the bar.
  //SetCookie("_ref", $page_name, time()+$_cookie_expire);

    // *** authorizing user ***

  freemed_open_db ($LoginCookie);  // authenticate user

    // *** initializing page ***

  freemed_display_html_top ();  // generate top of page
  freemed_display_banner ();    // display package banner

// *** main action loop ***
// (default action is "view")

switch($action) { // action switch
 case "add":
 case "mod":
 case "del": case "delete":
  switch($action) { // inner actionswitch
   case "add":
    freemed_display_box_top(_("Adding $record_name"), $page_name);
    $query = "INSERT INTO $db_name VALUES ( ".
      "'$phygroupname',  ".
      "'$phygroupfac',   ".
      "'$phygroupdtadd', ".
      "'$phygroupdtmod', ".
      " NULL ) ";
    echo "
      <P ALIGN=CENTER>
      <$STDFONT_B>"._("Adding").". . . 
    ";
   break;
   case "mod":
    freemed_display_box_top (_("Modifying $record_name"), $page_name);
    $query = "UPDATE $db_name SET ".
      "phygroupname  = '$phygroupname', ".
      "phygroupfac   = '$phygroupfac',  ". 
      "phygroupdtmod = '$cur_date'      ".
      "WHERE id='$id'";
    echo "
      <P ALIGN=CENTER>
      <$STDFONT_B>"._("Modifying").". . . 
    ";
   break;
   case "del": case "delete":
    freemed_display_box_top (_("Deleting $record_name"), $page_name);
    $query = "DELETE FROM $db_name WHERE (id = \"$id\")";
    echo "
      <P ALIGN=CENTER>
      <$STDFONT_B>"._("Deleting").". . . 
    ";
  } // inner action
 
  $result = fdb_query($query);

  if ($result) {
    echo "
      <B>"._("Done")."</B><$STDFONT_E>
    ";
  } else {
    echo ("<B>"._("Error")." ($result)</B><$STDFONT_E>\n"); 
  }
  echo "
    <P ALIGN=CENTER>
    <$STDFONT_B>
    <A HREF=\"$page_name?$_auth\">
      "._("Return to $record_name Menu")."
    </A>
    <$STDFONT_E>
  ";
  
  freemed_display_box_bottom (); // display the bottom of the box
 break;

 case "addform":
 case "modform":
 default:
  freemed_display_box_top (_("$record_name"), $page_name);
  $result = fdb_query("SELECT * FROM $db_name");
  
  echo freemed_display_itemlist(
    $result,
    $page_name,
    array (
      "Physician Group Name" => "phygroupname",
      "Default Facility"     => "phygroupfac"
    ),
    array ("",""),
    array (
      ""         => "",
      "facility" => "psrname"
    )
  ); // display main itemlist

  switch($action) { // inner action switch
   case "modform":
    if (strlen($id)<1) {
      $action="addform";
      break;
    }
    $r = freemed_get_link_rec($id,$db_name);
    $phygroupname = $r[phygroupname];
    $phygroupfac  = $r[phygroupfac];
   break;
   case "addform": // addform *is* the default
   default:
    // nothing right here...
   break;
  } // inner action switch
  
  $fac_r = fdb_query("SELECT * FROM facility ORDER BY psrname,psrnote");
  echo "
   <TABLE CELLSPACING=0 CELLPADDING=0 BORDER=0 WIDTH=\"100%\">
   <TR><TD ALIGN=CENTER>
    <FORM ACTION=\"$page_name\">
    <INPUT TYPE=HIDDEN NAME=\"action\" VALUE=\"".
      (($action=="modform") ? "mod" : "add")."\"> 
    <INPUT TYPE=HIDDEN NAME=\"id\"   VALUE=\"$id\"  >
   
    <$STDFONT_B>"._("Physician Group Name")." : <$STDFONT_E>
    <INPUT TYPE=TEXT NAME=phygroupname SIZE=20 MAXLENGTH=100
     VALUE=\"$phygroupname\">
   </TD></TR>

   <TR><TD ALIGN=CENTER>
    <$STDFONT_B>"._("Default Facility")." : <$STDFONT_E>
    ".freemed_display_selectbox($fac_r, "#psrname# [#psrnote#]", 
       "phygroupfac")."
   </TD></TR>
   
   <TR><TD ALIGN=CENTER>
    <INPUT TYPE=SUBMIT VALUE=\"".
      (($action=="modform") ? _("Modify") : _("Add"))."\">
    <INPUT TYPE=RESET  VALUE=\""._("Remove Changes")."\">
    </FORM>
   </TD></TR>
   </TABLE>
  ";
  if ($action=="modform")
    echo "
      <CENTER>
      <$STDFONT_B><A HREF=\"$page_name?$_auth&action=view\"
       >"._("Abandon Modification")."</A><$STDFONT_E>
      </CENTER>
    ";
  freemed_display_box_bottom (); // show the bottom of the box
 break;
} // master action switch

freemed_close_db(); // always close the database when done!
freemed_display_html_bottom (); // starting here, combined php3 code areas

?>
