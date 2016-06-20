# hippo-davstore-demo

Hippo CMS Demo using Jackrabbit VFSDataStore against either **WebDAV server** or **SFTP server** as a binary content storage.

Jackrabbit VFSDataStore is using Commons VFS 2.0 at the moment to get access to various backend storages such as local file system, WebDAV, SFTP, HDFS, etc.

Jackrabbit VFSDataStore was not merged into Apache Jackrabbit master branch, so you need to clone my feature branch, ```feature/vfs-datastore```,
from [https://github.com/woonsan/jackrabbit.git](https://github.com/woonsan/jackrabbit.git) and build/install it to make this demo run.

## Clone and Install VFSDataStore feature branch

You can clone my feature branch like the following:

        git clone -b feature/vfs-datastore https://github.com/woonsan/jackrabbit.git
        cd jackrabbit
        mvn clean install -DskipTests

## Build the Demo Project

This project uses the Maven to build.
From the project root folder, execute:

        mvn clean package

## Option 1: Install and Run an example WebDAV server

In this demo project, I used [WsgiDAV](https://github.com/mar10/wsgidav) server as WebDAV backend server.

You can install/configure it following this page:
- [http://wsgidav.readthedocs.io/en/latest/run-install.html](http://wsgidav.readthedocs.io/en/latest/run-install.html)
- [http://wsgidav.readthedocs.io/en/latest/run-configure.html](http://wsgidav.readthedocs.io/en/latest/run-configure.html)

If you installed it, you could move to ```wsgidav``` subfolder and run the following to make this demo run:

        cd wsgidav
        ./start-wsgidav.sh

The above command will start WebDAV server at port 8888 [http://localhost:8888](http://localhost:8888)
with the root directory at ```wsgidav/davshare```.

## Option 2: Using an SFTP server instead of WebDAV server

Open [pom.xml](pom.xml), and comment out the first ```<repo.config>``` element and uncomment the second one instead:

```xml
                  <repo.config>file://${project.basedir}/conf/repository-vfs2-webdav.xml</repo.config>
                  <!--
                  <repo.config>file://${project.basedir}/conf/repository-vfs2-sftp.xml</repo.config>
                  -->
```

And, open [repository-vfs2-sftp.xml](conf/repository-vfs2-sftp.xml) and edit the SFTP server URL:

```xml
          <DataStore class="org.apache.jackrabbit.vfs.ext.ds.VFSDataStore">
            <param name="baseFolderUri" value="sftp://tester:tester@localhost/vfsds" />
            <!-- SNIP -->
          </DataStore>
```

## Run the Demo Project

This project uses the Maven Cargo plugin to run CMS ("Content Authoring") and SITE ("Content Delivery") web applications locally in Tomcat.
From the project root folder, execute:

        mvn -P cargo.run

After your project is set up, access the CMS at http://localhost:8080/cms/ and the site at http://localhost:8080/site/.
Logs are located in target/tomcat7x/logs

If you take a look at the terminal of the WsgiDAV server, then you can already see many logs about content uploading. This is because Hippo Repository bootstraps some example binary content to the repository storage during the initialization.

## Repository Configuration

Repository configuration is located at the following:

- VFS2/WebDAV : [repository-vfs2-webdav.xml](conf/repository-vfs2-webdav.xml), which customizes the ```DataStore``` using ```VFSDataStore``` like the following:

```xml
          <DataStore class="org.apache.jackrabbit.vfs.ext.ds.VFSDataStore">
            <param name="baseFolderUri" value="webdav://tester:secret@localhost:8888/vfsds" />
            <param name="asyncWritePoolSize" value="10" />
            <param name="fileSystemOptionsPropertiesInString"
                   value="fso.http.maxTotalConnections = 200&#13;
                          fso.http.maxConnectionsPerHost = 200&#13;
                          fso.http.preemptiveAuth = false" />
            <param name="secret" value="123456789"/>
          </DataStore>
```

- VFS2/SFTP : [repository-vfs2-sftp.xml](conf/repository-vfs2-sftp.xml), which customizes the ```DataStore``` using ```VFSDataStore``` like the following:

```xml
          <DataStore class="org.apache.jackrabbit.vfs.ext.ds.VFSDataStore">
            <param name="baseFolderUri" value="sftp://tester:tester@localhost/vfsds" />
            <param name="asyncWritePoolSize" value="10" />
            <param name="fileSystemOptionsPropertiesInString"
                   value="" />
            <param name="secret" value="123456789"/>
          </DataStore>
```

## Test Scenarios

### Visit page retrieving binary images

- Visit http://localhost:8080/site/news.
- Click on a new article link.
- The photo inside the article was retrieved from the backend (WebDAV or SFTP) server through ```VFSDataStore``` component.
- You can see some logs in the WsgiDAV server when it's used.

### Visit CMS ("Content Authoring") to upload binary images / assets (e.g, pdfs)

- Visit http://localhost:8080/cms/
- Log on by admin/admin
- Click on the "Content" perspective.
- Select "Image" or "Asset" in the dropdown located near the left top corner.
- Click or create folder and try to upload image files or asset (e.g, pdfs) files.
- Tip: you need to hover your mouse on a tree node to find/click on action buttons.
- You can see some logs in the WsgiDAV server as well when it's used.
