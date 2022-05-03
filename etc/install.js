importPackage( Packages.org.openedit.util );
importPackage( Packages.java.util );
importPackage( Packages.java.lang );
importPackage( Packages.java.io );
importPackage( Packages.org.entermediadb.modules.update );




var name = "extension-hibernate";

var war = "http://dev.ijsolutions.ca/jenkins/job/" + name + "/lastSuccessfulBuild/artifact/deploy/" + name + ".zip";

var root = moduleManager.getBean("root").getAbsolutePath();
var web = root + "/WEB-INF";
var tmp = web + "/tmp";

log.info("1. GET THE LATEST WAR FILE");



log.info("1. GET THE LATEST WAR FILE");
var downloader = new Downloader();
downloader.download( war, tmp + "/extension-hibernate.zip");

log.info("2. UNZIP WAR FILE");
var unziper = new ZipUtil();
unziper.unzip(  tmp + "/extension-hibernate.zip",  tmp );


log.info("3. Copy Over Site " + tmp + "/unzip/" + " " + "to " + root);
var files = new FileUtils();



/*Hibernate */
files.deleteMatch( web + "lib/antlr*.jar");
files.deleteMatch( web + "lib/byte-buddy*.jar");
files.deleteMatch( web + "lib/classmate*.jar");
files.deleteMatch( web + "lib/FastInfoset*.jar");
files.deleteMatch( web + "lib/h2*.jar");
files.deleteMatch( web + "lib/hibernate-commons-annotations*.jar");
files.deleteMatch( web + "lib/hibernate-core-*.jar");
files.deleteMatch( web + "lib/istack-commons-*.jar");
files.deleteMatch( web + "lib/jandex-*.jar");
files.deleteMatch( web + "lib/javax.activation-api*.jar");
files.deleteMatch( web + "lib/javax.persistence-api*.jar");
files.deleteMatch( web + "lib/jaxb-api*.jar");
files.deleteMatch( web + "lib/jaxb-runtime*.jar");
files.deleteMatch( web + "lib/jboss-logging*.jar");
files.deleteMatch( web + "lib/jboss-transaction-api*.jar");
files.deleteMatch( web + "lib/mysql-connector-java*.jar");
files.deleteMatch( web + "lib/stax-ex*.jar");
files.deleteMatch( web + "lib/txw2*.jar");


files.copyFileByMatch( tmp + "/lib/antlr*.jar", web + "/lib/");  
files.copyFileByMatch( tmp + "/lib/byte-buddy*.jar", web + "/lib/");  
files.copyFileByMatch( tmp + "/lib/classmate*.jar", web + "/lib/");  
files.copyFileByMatch( tmp + "/lib/FastInfoset*.jar", web + "/lib/");  
files.copyFileByMatch( tmp + "/lib/h2*.jar", web + "/lib/");  
files.copyFileByMatch( tmp + "/lib/hibernate-commons-annotations*.jar", web + "/lib/");  
files.copyFileByMatch( tmp + "/lib/hibernate-core*.jar", web + "/lib/");  
files.copyFileByMatch( tmp + "/lib/istack-commons-runtime*.jar", web + "/lib/");  
files.copyFileByMatch( tmp + "/lib/jandex*.jar", web + "/lib/");  
files.copyFileByMatch( tmp + "/lib/javax.activation-api*.jar", web + "/lib/");  
files.copyFileByMatch( tmp + "/lib/javax.persistence-api*.jar", web + "/lib/");  
files.copyFileByMatch( tmp + "/lib/jaxb-api*.jar", web + "/lib/");  
files.copyFileByMatch( tmp + "/lib/jaxb-runtime*.jar", web + "/lib/");  
files.copyFileByMatch( tmp + "/lib/jboss-logging*.jar", web + "/lib/");  
files.copyFileByMatch( tmp + "/lib/jboss-transaction-api*.jar", web + "/lib/");  
files.copyFileByMatch( tmp + "/lib/mysql-connector-java*.jar", web + "/lib/");  
files.copyFileByMatch( tmp + "/lib/stax-ex*.jar", web + "/lib/");  
files.copyFileByMatch( tmp + "/lib/txw2*.jar", web + "/lib/");  

/*  End Hibernate */




files.deleteMatch( web + "/WEB-INF/base/hibernate/")
files.copyFileByMatch( tmp + "/base/hibernate/", root + "/WEB-INF/base/hibernate/");



log.info("5. CLEAN UP");
files.deleteAll(tmp);

log.info("6. UPGRADE COMPLETED");
