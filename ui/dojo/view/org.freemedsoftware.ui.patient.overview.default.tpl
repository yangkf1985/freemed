<!--{* Smarty *}-->
<!--{*
 // $Id$
 //
 // Authors:
 //      Jeff Buchbinder <jeff@freemedsoftware.org>
 //
 // Copyright (C) 1999-2007 FreeMED Software Foundation
 //
 // This program is free software; you can redistribute it and/or modify
 // it under the terms of the GNU General Public License as published by
 // the Free Software Foundation; either version 2 of the License, or
 // (at your option) any later version.
 //
 // This program is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 //
 // You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software
 // Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*}-->

<table border="0"><tr><td valign="top">
<!--{include file="org.freemedsoftware.widget.patientemrattachments.tpl" patient=$patient}-->
</td><td width="250" valign="top">
<!--{include file="org.freemedsoftware.widget.patienttags.tpl" patient=$patient}-->
<br clear="all" />
<!--{include file="org.freemedsoftware.widget.patientreferrals.tpl" patient=$patient}-->
</td></tr></table>
