# $Id$
#
# Authors:
#      Jeff Buchbinder <jeff@freemedsoftware.org>
#
# FreeMED Electronic Medical Record and Practice Management System
# Copyright (C) 1999-2012 FreeMED Software Foundation
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

# File: Monthly Client Balance Due

DROP PROCEDURE IF EXISTS report_MonthlyMasterJournalDetail_en_US;
DELIMITER //

# Function: report_MonthlyMasterJournalDetail_en_US
#
#	Monthly Report-Master Journal Detail
#

CREATE PROCEDURE report_MonthlyMasterJournalDetail_en_US ( IN startDate DATE,IN endDate DATE,IN facID INT)
BEGIN
	SET @sql = CONCAT(		
		"SELECT data.*,(SELECT pracname from practice ORDER BY id LIMIT 1) as practice FROM (",
		"(SELECT ",
			"1 as TypeID,",
			"'Sales Allowance' AS 'Type',",
			"'3rd Party Adj.' AS 'Category', ",
			"1 cattypeid, ",
			"IFNULL(i.insconame,'') AS thirdParty, ",
			"pr.payrecdtadd AS 'TransactionDate',",
			"IFNULL(DATE_FORMAT(MIN(p.procdt), '%m/%d/%Y'),'') AS 'dosstart', ",
			"IFNULL(DATE_FORMAT(MAX(p.procdtend), '%m/%d/%Y'),'') AS 'dosend', ",
			"p.procunits as Units,",
			"(SELECT IFNULL(SUM(pr1.payrecamt),0) FROM payrec pr1,procrec p1,coverage c1,insco i1 ",
				"WHERE pr1.payreccat IN (1,2,7,9,12) AND ",
				"p1.id=pr1.payrecproc AND ",
				"p1.proccurcovid !=0 AND ",
				"pr1.payrecdtadd=pr.payrecdtadd AND ",
				"p1.proccurcovid=c1.id AND ",
				"c1.covinsco=i1.id AND ",
				"i1.id=i.id AND ",
				"p1.procpos=",facID," AND ",
				"p1.procdtend>'",endDate,"' ) AS 'Deferred Amount',",
			"(SELECT IFNULL(SUM(pr1.payrecamt),0) FROM payrec pr1,procrec p1,coverage c1,insco i1 ",
				"WHERE pr1.payreccat IN (1,2,7,9,12) AND ",
				"p1.id=pr1.payrecproc AND ",
				"p1.proccurcovid !=0 AND ",
				"pr1.payrecdtadd=pr.payrecdtadd AND ",
				"p1.proccurcovid=c1.id AND ",
				"c1.covinsco=i1.id AND ",
				"i1.id=i.id AND ",
				"p1.procpos=",facID," AND ",
				"p1.procdtend<='",endDate,"' ) AS 'Actual Total',",
			"(SELECT IFNULL(SUM(pr1.payrecamt),0) FROM payrec pr1,procrec p1,coverage c1,insco i1 ",
				"WHERE pr1.payreccat IN (1,2,7,9,12) AND ",
				"p1.id=pr1.payrecproc AND ",
				"p1.proccurcovid !=0 AND ",
				"pr1.payrecdtadd=pr.payrecdtadd AND ",
				"p1.proccurcovid=c1.id AND ",
				"c1.covinsco=i1.id AND ",
				"i1.id=i.id AND ",
				"p1.procpos=",facID," AND ",
				"p1.procdtend<='",endDate,"' ) AS 'Batch Total',",
			"(SELECT IFNULL(SUM(pr1.payrecamt),0) FROM payrec pr1,procrec p1,coverage c1,insco i1 ",
				"WHERE pr1.payreccat IN (1,2,7,9,12) AND ",
				"p1.id=pr1.payrecproc AND ",
				"p1.proccurcovid !=0 AND ",
				"pr1.payrecdtadd=pr.payrecdtadd AND ",
				"p1.proccurcovid=c1.id AND ",
				"c1.covinsco=i1.id AND ",
				"i1.id=i.id AND ",
				"p1.procpos=",facID," AND ",
				"p1.procdtend<='",endDate,"' ) AS 'Date Total',",
			"f.psrname AS 'Facility Name' ",
		"FROM facility f,procrec p,payrec pr,coverage c, insco i ",
		"WHERE ",
			"pr.payreccat IN (1,2,7,9,12) AND ",
			"p.proccurcovid !=0 AND ",
			"p.id=pr.payrecproc AND ",
			"( (pr.payrecdtadd >= '",startDate,"' AND pr.payrecdtadd <= '",endDate,"') OR ",
			"(p.procdtend >= '",startDate,"' AND p.procdtend <= '",endDate,"' AND pr.payrecdtadd <= '",endDate,"') ) AND ",
			"c.id = p.proccurcovid AND ",
			"i.id = c.covinsco AND ",
			"p.procpos=",facID," AND ",
			"p.procpos=f.id GROUP BY thirdParty,TransactionDate ",
		" ) ",
		"UNION ALL ",
		"(SELECT ",
			"1 as TypeID,",
			"'Sales Allowance' AS 'Type',",
			"'Non 3rd Party Adj.' AS 'Category', ",
			"2 cattypeid, ",
			"'' AS thirdParty, ",
			"pr.payrecdtadd AS 'TransactionDate',",
			"IFNULL(DATE_FORMAT(MIN(p.procdt), '%m/%d/%Y'),'') AS 'dosstart', ",
			"IFNULL(DATE_FORMAT(MAX(p.procdtend), '%m/%d/%Y'),'') AS 'dosend', ",
			"p.procunits as Units,",
			"(SELECT IFNULL(SUM(pr1.payrecamt),0) FROM payrec pr1,procrec p1 ",
				"WHERE pr1.payreccat IN (1,2,7,9,12) AND ",
				"p1.id=pr1.payrecproc AND ",
				"p1.proccurcovid =0 AND ",
				"pr1.payrecdtadd=pr.payrecdtadd AND ",
				"p1.procpos=",facID," AND ",
				"p1.procdtend>'",endDate,"' ) AS 'Deferred Amount',",
			"(SELECT IFNULL(SUM(pr1.payrecamt),0) FROM payrec pr1,procrec p1 ",
				"WHERE pr1.payreccat IN (1,2,7,9,12) AND ",
				"p1.id=pr1.payrecproc AND ",
				"p1.proccurcovid =0 AND ",
				"pr1.payrecdtadd=pr.payrecdtadd AND ",
				"p1.procpos=",facID," AND ",
				"p1.procdtend<='",endDate,"' ) AS 'Actual Total',",
			"(SELECT IFNULL(SUM(pr1.payrecamt),0) FROM payrec pr1,procrec p1 ",
				"WHERE pr1.payreccat IN (1,2,7,9,12) AND ",
				"p1.id=pr1.payrecproc AND ",
				"p1.proccurcovid =0 AND ",
				"pr1.payrecdtadd=pr.payrecdtadd AND ",
				"p1.procpos=",facID," AND ",
				"p1.procdtend<='",endDate,"' ) AS 'Batch Total',",
			"(SELECT IFNULL(SUM(pr1.payrecamt),0) FROM payrec pr1,procrec p1 ",
				"WHERE pr1.payreccat IN (1,2,7,9,12) AND ",
				"p1.id=pr1.payrecproc AND ",
				"p1.proccurcovid =0 AND ",
				"pr1.payrecdtadd=pr.payrecdtadd AND ",
				"p1.procpos=",facID," AND ",
				"p1.procdtend<='",endDate,"' ) AS 'Date Total',",
			"f.psrname AS 'Facility Name' ",
		"FROM facility f,procrec p,payrec pr ",
		"WHERE ",
			"pr.payreccat IN (1,2,7,9,12) AND ",
			"p.proccurcovid =0 AND ",
			"p.id=pr.payrecproc AND ",
			"( (pr.payrecdtadd >= '",startDate,"' AND pr.payrecdtadd <= '",endDate,"') OR ",
			"(p.procdtend >= '",startDate,"' AND p.procdtend <= '",endDate,"' AND pr.payrecdtadd <= '",endDate,"') ) AND ",
			"p.procpos=",facID," AND ",
			"p.procpos=f.id GROUP BY thirdParty,TransactionDate ",
		" ) ",
		"UNION ALL ",
		"(SELECT ",
			"2 as TypeID,",
			"'Sales' AS 'Type',",
			"'3rd Party Adj.' AS 'Category', ",
			"1 AS cattypeid, ",
			"IFNULL(i.insconame,'') AS thirdParty, ",
			"pr.payrecdtadd AS 'TransactionDate',",
			"IFNULL(DATE_FORMAT(MIN(p.procdt), '%m/%d/%Y'),'') AS 'dosstart', ",
			"IFNULL(DATE_FORMAT(MAX(p.procdtend), '%m/%d/%Y'),'') AS 'dosend', ",
			"p.procunits as Units,",
			"(SELECT IFNULL(SUM(p1.proccharges),0) FROM payrec pr1,procrec p1,coverage c1,insco i1 ",
				"WHERE pr1.payreccat=5 AND ",
				"p1.id=pr1.payrecproc AND ",
				"p1.proccurcovid!=0 AND ",
				"pr1.payrecdtadd=pr.payrecdtadd AND ",
				"p1.proccurcovid=c1.id AND ",
				"c1.covinsco=i1.id AND ",
				"i1.id=i.id AND ",
				"p1.procpos=",facID," AND ",
				"p1.procdtend>'",endDate,"' ) AS 'Deferred Amount',",
			"(SELECT IFNULL(SUM(p1.procbalorig),0) FROM payrec pr1,procrec p1,coverage c1,insco i1 ",
				"WHERE pr1.payreccat=5 AND ",
				"p1.id=pr1.payrecproc AND ",
				"p1.proccurcovid!=0 AND ",
				"pr1.payrecdtadd=pr.payrecdtadd AND ",
				"p1.proccurcovid=c1.id AND ",
				"c1.covinsco=i1.id AND ",
				"i1.id=i.id AND ",
				"p1.procpos=",facID," AND ",
				"p1.procdtend<='",endDate,"' ) AS 'Actual Total',",
			"(SELECT IFNULL(SUM(p1.proccharges),0) FROM payrec pr1,procrec p1,coverage c1,insco i1 ",
				"WHERE pr1.payreccat=5 AND ",
				"p1.id=pr1.payrecproc AND ",
				"p1.proccurcovid!=0 AND ",
				"pr1.payrecdtadd=pr.payrecdtadd AND ",
				"p1.proccurcovid=c1.id AND ",
				"c1.covinsco=i1.id AND ",
				"i1.id=i.id AND ",
				"p1.procpos=",facID," AND ",
				"p1.procdtend<='",endDate,"' ) AS 'Batch Total',",
			"(SELECT IFNULL(SUM(p1.proccharges),0) FROM payrec pr1,procrec p1,coverage c1,insco i1 ",
				"WHERE pr1.payreccat=5 AND ",
				"p1.id=pr1.payrecproc AND ",
				"p1.proccurcovid!=0 AND ",
				"pr1.payrecdtadd=pr.payrecdtadd AND ",
				"p1.proccurcovid=c1.id AND ",
				"c1.covinsco=i1.id AND ",
				"i1.id=i.id AND ",
				"p1.procpos=",facID," AND ",
				"p1.procdtend<='",endDate,"' ) AS 'Date Total',",
			"f.psrname AS 'Facility Name' ",
		"FROM facility f,payrec pr,procrec p ",
			"LEFT OUTER JOIN coverage c ON c.id = p.proccurcovid  ",
			"LEFT OUTER JOIN insco i ON i.id = c.covinsco ",
		"WHERE ",
			"pr.payrecproc = p.id AND pr.payreccat=5 AND p.proccurcovid!=0 AND ",
			"( (pr.payrecdtadd >= '",startDate,"' AND pr.payrecdtadd <= '",endDate,"') OR ",
			"(p.procdtend >= '",startDate,"' AND p.procdtend <= '",endDate,"' AND pr.payrecdtadd <= '",endDate,"') ) AND ",
			"p.procpos=",facID," AND ",
			"p.procpos=f.id GROUP BY thirdParty,TransactionDate ",
		") ",
		"UNION ALL ",
		"(SELECT ",
			"2 as TypeID,"
			"'Sales' AS 'Type',"
			"'Lab Fee' AS 'Category', ",
			"2 AS 'cattypeid', ",
			"'' AS thirdParty, ",
			"pr.payrecdtadd AS 'TransactionDate',",
			"IFNULL(DATE_FORMAT(MIN(p.procdt), '%m/%d/%Y'),'') AS 'dosstart', ",
			"IFNULL(DATE_FORMAT(MAX(p.procdtend), '%m/%d/%Y'),'') AS 'dosend', ",
			"1 as Units,",
			"(SELECT IFNULL(SUM(p1.proclabcharges),0) FROM payrec pr1,procrec p1 ",
				"WHERE pr1.payreccat=5 AND ",
				"p1.id=pr1.payrecproc AND ",
				"p1.proclabcharges>0 AND ",
				"pr1.payrecdtadd=pr.payrecdtadd AND ",
				"p1.procpos=",facID," AND ",
				"p1.procdtend>'",endDate,"' ) AS 'Deferred Amount',",
			"(SELECT IFNULL(SUM(p1.proclabcharges),0) FROM payrec pr1,procrec p1 ",
				"WHERE pr1.payreccat=5 AND ",
				"p1.id=pr1.payrecproc AND ",
				"p1.proclabcharges>0 AND ",
				"pr1.payrecdtadd=pr.payrecdtadd AND ",
				"p1.procpos=",facID," AND ",
				"p1.procdtend<='",endDate,"' ) AS 'Actual Total',",
			"(SELECT IFNULL(SUM(p1.proclabcharges),0) FROM payrec pr1,procrec p1 ",
				"WHERE pr1.payreccat=5 AND ",
				"p1.id=pr1.payrecproc AND ",
				"p1.proclabcharges>0 AND ",
				"pr1.payrecdtadd=pr.payrecdtadd AND ",
				"p1.procpos=",facID," AND ",
				"p1.procdtend<='",endDate,"' ) AS 'Batch Total',",
			"(SELECT IFNULL(SUM(p1.proclabcharges),0) FROM payrec pr1,procrec p1 ",
				"WHERE pr1.payreccat=5 AND ",
				"p1.id=pr1.payrecproc AND ",
				"p1.proclabcharges>0 AND ",
				"pr1.payrecdtadd=pr.payrecdtadd AND ",
				"p1.procpos=",facID," AND ",
				"p1.procdtend<='",endDate,"' ) AS 'Date Total',",
			"f.psrname AS 'Facility Name' ",
		"FROM facility f,payrec pr,procrec p ",
			"LEFT OUTER JOIN coverage c ON c.id = p.proccurcovid  ",
			"LEFT OUTER JOIN insco i ON i.id = c.covinsco ",
		"WHERE ",
			"pr.payrecproc = p.id AND pr.payreccat=5 AND ",
			"( (pr.payrecdtadd >= '",startDate,"' AND pr.payrecdtadd <= '",endDate,"') OR ",
			"(p.procdtend >= '",startDate,"' AND p.procdtend <= '",endDate,"' AND pr.payrecdtadd <= '",endDate,"') ) AND ",
			"p.proclabcharges>0 AND ",
			"p.procpos=",facID," AND ",
			"p.procpos=f.id GROUP BY TransactionDate ",
		" ) ",
		"UNION ALL ",
		"(SELECT ",
			"2 as TypeID,",
			"'Sales' AS 'Type',",
			"'Self Pay' AS 'Category', ",
			"3 AS cattypeid, ",
			"'' AS thirdParty, ",
			"pr.payrecdtadd AS 'TransactionDate',",
			"IFNULL(DATE_FORMAT(MIN(p.procdt), '%m/%d/%Y'),'') AS 'dosstart', ",
			"IFNULL(DATE_FORMAT(MAX(p.procdtend), '%m/%d/%Y'),'') AS 'dosend', ",
			"p.procunits as Units,",
			"(SELECT IFNULL(SUM(p1.proccharges),0) FROM payrec pr1,procrec p1 ",
				"WHERE pr1.payreccat=5 AND ",
				"p1.id=pr1.payrecproc AND ",
				"p1.proccurcovid=0 AND ",
				"pr1.payrecdtadd=pr.payrecdtadd AND ",
				"p1.procpos=",facID," AND ",
				"p1.procdtend>'",endDate,"' ) AS 'Deferred Amount',",
			"(SELECT IFNULL(SUM(p1.procbalorig),0) FROM payrec pr1,procrec p1 ",
				"WHERE pr1.payreccat=5 AND ",
				"p1.id=pr1.payrecproc AND ",
				"p1.proccurcovid=0 AND ",
				"pr1.payrecdtadd=pr.payrecdtadd AND ",
				"p1.procpos=",facID," AND ",
				"p1.procdtend<='",endDate,"' ) AS 'Actual Total',",
			"(SELECT IFNULL(SUM(p1.proccharges),0) FROM payrec pr1,procrec p1 ",
				"WHERE pr1.payreccat=5 AND ",
				"p1.id=pr1.payrecproc AND ",
				"p1.proccurcovid=0 AND ",
				"pr1.payrecdtadd=pr.payrecdtadd AND ",
				"p1.procpos=",facID," AND ",
				"p1.procdtend<='",endDate,"' ) AS 'Batch Total',",
			"(SELECT IFNULL(SUM(p1.proccharges),0) FROM payrec pr1,procrec p1 ",
				"WHERE pr1.payreccat=5 AND ",
				"p1.id=pr1.payrecproc AND ",
				"p1.proccurcovid=0 AND ",
				"pr1.payrecdtadd=pr.payrecdtadd AND ",
				"p1.procpos=",facID," AND ",
				"p1.procdtend<='",endDate,"' ) AS 'Date Total',",
			"f.psrname AS 'Facility Name' ",
		"FROM facility f,payrec pr,procrec p ",
			"LEFT OUTER JOIN coverage c ON c.id = p.proccurcovid  ",
			"LEFT OUTER JOIN insco i ON i.id = c.covinsco ",
		"WHERE ",
			"pr.payrecproc = p.id AND pr.payreccat=5 AND p.proccurcovid=0 AND ",
			"( (pr.payrecdtadd >= '",startDate,"' AND pr.payrecdtadd <= '",endDate,"') OR ",
			"(p.procdtend >= '",startDate,"' AND p.procdtend <= '",endDate,"' AND pr.payrecdtadd <= '",endDate,"') ) AND ",
			"p.procpos=",facID," AND ",
			"p.procpos=f.id  GROUP BY thirdParty,TransactionDate ",
		" ) ",
		"UNION ALL ",	
		#3rd party
		"(SELECT ",
			"3 as TypeID,"
			"'Cash Receipts' AS 'Type',"
			"'3rd Party Pmt' AS 'Category', ",
			"1 AS 'cattypeid', ",
			"i.insconame AS thirdParty, ",
			"pr.payrecdtadd AS 'TransactionDate',",
			"IFNULL(DATE_FORMAT(MIN(p.procdt), '%m/%d/%Y'),'') AS 'dosstart', ",
			"IFNULL(DATE_FORMAT(MAX(p.procdtend), '%m/%d/%Y'),'') AS 'dosend', ",
			"1 as Units,",
			"(SELECT IFNULL(SUM(pr1.payrecamt),0) FROM payrec pr1,procrec p1,cpt cp1,coverage c1,insco i1  ",
				"WHERE pr1.payreccat=0 AND ",
				"pr1.payrecdtadd=pr.payrecdtadd AND ",
				"pr1.payreclink!=0 AND ",
				"pr1.payrecproc=p1.id AND ",
				"cp1.id = p1.proccpt AND ",
				"cp1.cptcode!='80101' AND ",
				"pr1.payreclink=c1.id AND ",
				"c1.covinsco=i1.id AND ",
				"i1.id=i.id AND ",
				"p1.procpos=",facID," AND ",
				"p1.procdtend>'",endDate,"' ) AS 'Deferred Amount',",
			"(SELECT IFNULL(SUM(pr1.payrecamt),0) FROM payrec pr1,procrec p1,cpt cp1,coverage c1,insco i1  ",
				"WHERE pr1.payreccat=0 AND ",
				"pr1.payrecdtadd=pr.payrecdtadd AND ",
				"pr1.payreclink!=0 AND ",
				"pr1.payrecproc=p1.id AND ",
				"cp1.id = p1.proccpt AND ",
				"cp1.cptcode!='80101' AND ",
				"pr1.payreclink=c1.id AND ",
				"c1.covinsco=i1.id AND ",
				"i1.id=i.id AND ",
				"p1.procpos=",facID," AND ",
				"p1.procdtend<='",endDate,"' ) AS 'Actual Total',",
			"(SELECT IFNULL(SUM(pr1.payrecamt),0) FROM payrec pr1,procrec p1,cpt cp1,coverage c1,insco i1 ",
				"WHERE pr1.payreccat=0 AND ",
				"pr1.payrecdtadd=pr.payrecdtadd AND ",
				"pr1.payreclink!=0 AND ",
				"pr1.payrecproc=p1.id AND ",
				"cp1.id = p1.proccpt AND ",
				"cp1.cptcode!='80101' AND ",
				"pr1.payreclink=c1.id AND ",
				"c1.covinsco=i1.id AND ",
				"i1.id=i.id AND ",
				"p1.procpos=",facID," AND ",
				"p1.procdtend<='",endDate,"' ) AS 'Batch Total',",
			"(SELECT IFNULL(SUM(pr1.payrecamt),0) FROM payrec pr1,procrec p1,cpt cp1,coverage c1,insco i1  ",
				"WHERE pr1.payreccat=0 AND ",
				"pr1.payrecdtadd=pr.payrecdtadd AND ",
				"pr1.payreclink!=0 AND ",
				"pr1.payrecproc=p1.id AND ",
				"cp1.id = p1.proccpt AND ",
				"cp1.cptcode!='80101' AND ",
				"pr1.payreclink=c1.id AND ",
				"c1.covinsco=i1.id AND ",
				"i1.id=i.id AND ",
				"p1.procpos=",facID," AND ",
				"p1.procdtend<='",endDate,"' ) AS 'Date Total',",
			"f.psrname AS 'Facility Name' ",
		"FROM facility f,payrec pr,procrec p ",
			"LEFT OUTER JOIN coverage c ON c.id = p.proccurcovid  ",
			"LEFT OUTER JOIN insco i ON i.id = c.covinsco ",
			"LEFT OUTER JOIN cpt cp ON cp.id = p.proccpt ",
		"WHERE ",
			"pr.payrecproc=p.id AND ",
			"pr.payreccat=0 AND ",
			"pr.payreclink!=0 AND ",
			"( (pr.payrecdtadd >= '",startDate,"' AND pr.payrecdtadd <= '",endDate,"') OR ",
			"(p.procdtend >= '",startDate,"' AND p.procdtend <= '",endDate,"' AND pr.payrecdtadd <= '",endDate,"') ) AND ",
			"cp.id = p.proccpt AND ",
			"cp.cptcode!='80101' AND "
			"p.procpos=",facID," AND ",
			"p.procpos=f.id  GROUP BY TransactionDate ",
		")",
		
		"UNION ALL ",
		#CASH
		"(SELECT ",
			"3 as TypeID,"
			"'Cash Receipts' AS 'Type',"
			"'Cash' AS 'Category', ",
			"2 AS 'cattypeid', ",
			"'' AS thirdParty, ",
			"pr.payrecdtadd AS 'TransactionDate',",
			"IFNULL(DATE_FORMAT(MIN(p.procdt), '%m/%d/%Y'),'') AS 'dosstart', ",
			"IFNULL(DATE_FORMAT(MAX(p.procdtend), '%m/%d/%Y'),'') AS 'dosend', ",
			"1 as Units,",
			"(SELECT IFNULL(SUM(pr1.payrecamt),0) FROM payrec pr1,procrec p1,cpt cp1 ",
				"WHERE pr1.payreccat=0 AND ",
				"pr1.payrecdtadd=pr.payrecdtadd AND ",
				"pr1.payreclink=0 AND ",
				"pr1.payrecproc=p1.id AND ",
				"cp1.id = p1.proccpt AND ",
				"cp1.cptcode!='80101' AND ",
				"p1.procpos=",facID," AND ",
				"p1.procdtend>'",endDate,"' ) AS 'Deferred Amount',",
			"(SELECT IFNULL(SUM(pr1.payrecamt),0) FROM payrec pr1,procrec p1,cpt cp1 ",
				"WHERE pr1.payreccat=0 AND ",
				"pr1.payrecdtadd=pr.payrecdtadd AND ",
				"pr1.payreclink=0 AND ",
				"pr1.payrecproc=p1.id AND ",
				"cp1.id = p1.proccpt AND ",
				"cp1.cptcode!='80101' AND ",
				"p1.procpos=",facID," AND ",
				"p1.procdtend<='",endDate,"' ) AS 'Actual Total',",
			"(SELECT IFNULL(SUM(pr1.payrecamt),0) FROM payrec pr1,procrec p1,cpt cp1 ",
				"WHERE pr1.payreccat=0 AND ",
				"pr1.payrecdtadd=pr.payrecdtadd AND ",
				"pr1.payreclink=0 AND ",
				"pr1.payrecproc=p1.id AND ",
				"cp1.id = p1.proccpt AND ",
				"cp1.cptcode!='80101' AND ",
				"p1.procpos=",facID," AND ",
				"p1.procdtend<='",endDate,"' ) AS 'Batch Total',",
			"(SELECT IFNULL(SUM(pr1.payrecamt),0) FROM payrec pr1,procrec p1,cpt cp1 ",
				"WHERE pr1.payreccat=0 AND ",
				"pr1.payrecdtadd=pr.payrecdtadd AND ",
				"pr1.payreclink=0 AND ",
				"pr1.payrecproc=p1.id AND ",
				"cp1.id = p1.proccpt AND ",
				"cp1.cptcode!='80101' AND ",
				"p1.procpos=",facID," AND ",
				"p1.procdtend<='",endDate,"' ) 'Date Total',",
			"f.psrname AS 'Facility Name' ",
		"FROM facility f,payrec pr,procrec p,cpt cp ",
		"WHERE ",
			"pr.payrecproc=p.id AND ",
			"cp.id = p.proccpt AND ",
			"pr.payreccat=0 AND ",
			"pr.payreclink=0 AND ",
			"( (pr.payrecdtadd >= '",startDate,"' AND pr.payrecdtadd <= '",endDate,"') OR ",
			"(p.procdtend >= '",startDate,"' AND p.procdtend <= '",endDate,"' AND pr.payrecdtadd <= '",endDate,"') ) AND ",
			"cp.cptcode!='80101' AND "
			"p.procpos=",facID," AND ",
			"p.procpos=f.id GROUP BY TransactionDate ",
		") ",
		"UNION ALL ",
		#Copay/Deductables
		"(SELECT ",
			"3 as TypeID,"
			"'Cash Receipts' AS 'Type',"
			"'Copay/Deductable' AS 'Category', ",
			"3 AS 'cattypeid', ",
			"'' AS thirdParty, ",
			"pr.payrecdtadd AS 'TransactionDate',",
			"IFNULL(DATE_FORMAT(MIN(p.procdt), '%m/%d/%Y'),'') AS 'dosstart', ",
			"IFNULL(DATE_FORMAT(MAX(p.procdtend), '%m/%d/%Y'),'') AS 'dosend', ",
			"1 as Units,",
			"(SELECT IFNULL(SUM(pr1.payrecamt),0) FROM payrec pr1,procrec p1 ",
				"WHERE pr1.payreccat IN (8,11) AND ",
				"pr1.payrecdtadd=pr.payrecdtadd AND ",
				"pr1.payrecproc=p1.id AND ",	
				"p1.procpos=",facID," AND ",
				"p1.procdtend>'",endDate,"' ) AS 'Deferred Amount',",
			"(SELECT IFNULL(SUM(pr1.payrecamt),0) FROM payrec pr1,procrec p1 ",
				"WHERE pr1.payreccat IN (8,11) AND ",
				"pr1.payrecdtadd=pr.payrecdtadd AND ",
				"pr1.payrecproc=p1.id AND ",
				"p1.procpos=",facID," AND ",
				"p1.procdtend<='",endDate,"' ) AS 'Actual Total',",
			"(SELECT IFNULL(SUM(pr1.payrecamt),0) FROM payrec pr1,procrec p1 ",
				"WHERE pr1.payreccat IN (8,11) AND ",
				"pr1.payrecdtadd=pr.payrecdtadd AND ",
				"pr1.payrecproc=p1.id AND ",
				"p1.procpos=",facID," AND ",
				"p1.procdtend<='",endDate,"' ) AS 'Batch Total',",
			"(SELECT IFNULL(SUM(pr1.payrecamt),0) FROM payrec pr1,procrec p1 ",
				"WHERE pr1.payreccat IN (8,11) AND ",
				"pr1.payrecdtadd=pr.payrecdtadd AND ",
				"pr1.payrecproc=p1.id AND ",
				"p1.procpos=",facID," AND ",
				"p1.procdtend<='",endDate,"' ) 'Date Total',",
			"f.psrname AS 'Facility Name' ",
		"FROM facility f,payrec pr,procrec p,cpt cp ",
		"WHERE ",
			"pr.payrecproc=p.id AND ",
			"cp.id = p.proccpt AND ",
			"pr.payreccat IN (8,11) AND ",
			"( (pr.payrecdtadd >= '",startDate,"' AND pr.payrecdtadd <= '",endDate,"') OR ",
			"(p.procdtend >= '",startDate,"' AND p.procdtend <= '",endDate,"' AND pr.payrecdtadd <= '",endDate,"') ) AND ",
			"p.procpos=",facID," AND ",
			"p.procpos=f.id GROUP BY TransactionDate ",
		") "
		"UNION ALL ",
		#UA/BSN
		"(SELECT ",
			"3 as TypeID,"
			"'Cash Receipts' AS 'Type',"
			"'UA/BSN' AS 'Category', ",
			"4 AS 'cattypeid', ",
			"'' AS thirdParty, ",
			"pr.payrecdtadd AS 'TransactionDate',",
			"IFNULL(DATE_FORMAT(MIN(p.procdt), '%m/%d/%Y'),'') AS 'dosstart', ",
			"IFNULL(DATE_FORMAT(MAX(p.procdtend), '%m/%d/%Y'),'') AS 'dosend', ",
			"1 as Units,",
			"(SELECT IFNULL(SUM(pr1.payrecamt),0) FROM payrec pr1,procrec p1,cpt cp1 ",
				"WHERE pr1.payreccat=0 AND ",
				"pr1.payrecdtadd=pr.payrecdtadd AND ",
				"pr1.payrecproc=p1.id AND ",
				"cp1.id = p1.proccpt AND ",
				"cp1.cptcode='80101' AND ",
				"p1.procpos=",facID," AND ",
				"p1.procdtend>'",endDate,"' ) AS 'Deferred Amount',",
			"(SELECT IFNULL(SUM(pr1.payrecamt),0) FROM payrec pr1,procrec p1,cpt cp1 ",
				"WHERE pr1.payreccat=0 AND ",
				"pr1.payrecdtadd=pr.payrecdtadd AND ",
				"pr1.payrecproc=p1.id AND ",
				"cp1.id = p1.proccpt AND ",
				"cp1.cptcode='80101' AND ",
				"p1.procpos=",facID," AND ",
				"p1.procdtend<='",endDate,"' ) AS 'Actual Total',",
			"(SELECT IFNULL(SUM(pr1.payrecamt),0) FROM payrec pr1,procrec p1,cpt cp1 ",
				"WHERE pr1.payreccat=0 AND ",
				"pr1.payrecdtadd=pr.payrecdtadd AND ",
				"pr1.payrecproc=p1.id AND ",
				"cp1.id = p1.proccpt AND ",
				"cp1.cptcode='80101' AND ",
				"p1.procpos=",facID," AND ",
				"p1.procdtend<='",endDate,"' ) AS 'Batch Total',",
			"(SELECT IFNULL(SUM(pr1.payrecamt),0) FROM payrec pr1,procrec p1,cpt cp1 ",
				"WHERE pr1.payreccat=0 AND ",
				"pr1.payrecdtadd=pr.payrecdtadd AND ",
				"pr1.payrecproc=p1.id AND ",
				"cp1.id = p1.proccpt AND ",
				"cp1.cptcode='80101' AND ",
				"p1.procpos=",facID," AND ",
				"p1.procdtend<='",endDate,"' ) 'Date Total',",
			"f.psrname AS 'Facility Name' ",
		"FROM facility f,payrec pr,procrec p,cpt cp ",
		"WHERE ",
			"pr.payrecproc=p.id AND ",
			"cp.id = p.proccpt AND ",
			"pr.payreccat=0 AND ",
			"( (pr.payrecdtadd >= '",startDate,"' AND pr.payrecdtadd <= '",endDate,"') OR ",
			"(p.procdtend >= '",startDate,"' AND p.procdtend <= '",endDate,"' AND pr.payrecdtadd <= '",endDate,"') ) AND ",
			"cp.cptcode='80101' AND "
			"p.procpos=",facID," AND ",
			"p.procpos=f.id GROUP BY TransactionDate ",
		") ) data ORDER BY data.TypeID,data.cattypeid,data.thirdParty,TransactionDate"
	);
	PREPARE s FROM @sql ;
	EXECUTE s ;
	DEALLOCATE PREPARE s ;
END
//
DELIMITER ;

#	Add indices

DELETE FROM `reporting` WHERE report_sp = 'report_MonthlyMasterJournalDetail_en_US';
INSERT INTO `reporting` (
		report_name,
		report_uuid,
		report_locale,
		report_desc,
		report_type,
		report_category,
		report_sp,
		report_param_count,
		report_param_names,
		report_param_types,
		report_param_optional,
		report_formatting
	) VALUES (
		'Monthly Report-Master Journal Detail',
		'09f28b6c-9007-46fb-bec5-bf3afd2416c1',
		'en_US',
		'Monthly Report-Master Journal Detail',
		'jasper',
		'reporting_engine',
		'report_MonthlyMasterJournalDetail_en_US',
		3,
		'Start Date, End Date,Facility',
		'Date,Date,Facility',
		'0,0,0',
		'MonthlyMasterJournalDetail_en_US'
	);

