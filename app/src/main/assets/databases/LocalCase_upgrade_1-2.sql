-- add a FullNames column to Employees

DROP TABLE IF EXISTS "T_Case";
CREATE TABLE "T_Case" ("CaseID" INTEGER PRIMARY KEY  NOT NULL ,"InspectorID" VARCHAR,"DisposalID" VARCHAR,"AuditorID" VARCHAR,"CaseType" VARCHAR,"CaseLargeClass" VARCHAR,"CaseSmallClass" VARCHAR,"CaseSubClass" VARCHAR,"ProcessStage" VARCHAR,"ProcessResult" VARCHAR,"CreateTime" VARCHAR,"PostTime" VARCHAR,"ReceiveTime" VARCHAR,"Address" VARCHAR,"CaseDesc" VARCHAR,"X" DOUBLE,"Y" DOUBLE,"Shape" VARCHAR,"WorkFlowID" VARCHAR,"GridID" VARCHAR,"Status" VARCHAR,"ProblemID" VARCHAR,"CmdID" VARCHAR,"ThingId" VARCHAR DEFAULT (null) ,"LayerId" VARCHAR DEFAULT (null) ,"CaseCondition" VARCHAR DEFAULT (null) ,"Emergency" VARCHAR DEFAULT (null));
