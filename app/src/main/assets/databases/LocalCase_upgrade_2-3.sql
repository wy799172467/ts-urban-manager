--CREATE TABLE "T_Layers"
DROP TABLE IF EXISTS "T_Layers";
CREATE TABLE "T_Layers" ("_id" INTEGER PRIMARY KEY  NOT NULL ,"layer_id" VARCHAR,"name" VARCHAR,"category" VARCHAR,"url" VARCHAR,"LayerIds" VARCHAR,"TokenName" VARCHAR,"offline_name" VARCHAR,"offline_url" VARCHAR,"offline_time" INTEGER,"offline_size" DOUBLE,"alpha" FLOAT,"isDownloaded" BOOL,"hasUpdate" BOOL);
