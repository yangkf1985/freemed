# $Id$
#
# Authors:
#      Jeff Buchbinder <jeff@freemedsoftware.org>
#
# FreeMED Electronic Medical Record and Practice Management System
# Copyright (C) 1999-2006 FreeMED Software Foundation
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

SOURCE data/schema/mysql/patient.sql

CREATE TABLE IF NOT EXISTS `form_results` (
	fr_patient		INT UNSIGNED NOT NULL,
	fr_timestamp		TIMESTAMP (16) NOT NULL DEFAULT NOW(),
	fr_template		VARCHAR (50),
	fr_formname		VARCHAR (50),
	id			INT UNSIGNED NOT NULL AUTO_INCREMENT,
	PRIMARY KEY 		( id ),

	#	Define keys

	KEY			( fr_patient, fr_timestamp ),
	FOREIGN KEY		( fr_patient ) REFERENCES patient ( id ) ON DELETE CASCADE
) ENGINE=InnoDB;
