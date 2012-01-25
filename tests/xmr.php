<?php
 // $Id$
 //
 // Authors:
 //      Jeff Buchbinder <jeff@freemedsoftware.org>
 //
 // FreeMED Electronic Medical Record and Practice Management System
 // Copyright (C) 1999-2012 FreeMED Software Foundation
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

include_once ( dirname(__FILE__).'/bootstrap.test.php' );

$db = CreateObject('org.freemedsoftware.core.FreemedDb');

print " - Creating form ... \n";

t("org.freemedsoftware.module.XmrDefinition.del(1)",
	CallMethod(
		'org.freemedsoftware.module.XmrDefinition.del',
		array(
			'id' => 1
		)
	)
);
t("org.freemedsoftware.module.XmrDefinition.add(1)",
	CallMethod(
		'org.freemedsoftware.module.XmrDefinition.add',
		array(
			  'form_name' => 'XMR Test'
			, 'form_definition' => 'Regression test'
			, 'form_locale' => 'en_US'
			, 'form_template' => ''
			, 'id' => 1
		)
	)
);
$db->queryAll( "UPDATE xmr_definition SET id = 1 WHERE form_name='XMR Test';" );

print " - Removing old elements ... \n";

$db->queryAll( "DELETE FROM xmr_definition_element WHERE form_id = 1;" );

print " - Adding new elements for definition ... \n";

$elements = array (
	array (
		  'form_id' => 1
		, 'text_name' => ''
		, 'parent_concept_id' => ''
		, 'concept_id' => ''
		, 'quant_id' => ''
		, 'external_population' => 0
		, 'id' => 0
	), array (
		  'form_id' => 1
		, 'text_name' => ''
		, 'parent_concept_id' => ''
		, 'concept_id' => ''
		, 'quant_id' => ''
		, 'external_population' => 0
		, 'id' => 0
	), array (
		  'form_id' => 1
		, 'text_name' => ''
		, 'parent_concept_id' => ''
		, 'concept_id' => ''
		, 'quant_id' => ''
		, 'external_population' => 0
		, 'id' => 0
	)
);

t("org.freemedsoftware.module.XmrDefinition.SetElements",
	CallMethod(
		'org.freemedsoftware.module.XmrDefinition.SetElements'
		$elements
	)
);

?>
