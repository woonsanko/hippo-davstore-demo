# hippo-davstore-demo

Hippo CMS Demo using Jackrabbit ```VFSDataStore``` against either **WebDAV server** or **SFTP server** as a binary content storage.

Jackrabbit ```VFSDataStore``` (available since Jackrabbit 2.13.2) is using Commons VFS 2 to get access to various backend storages such as local file system, WebDAV, SFTP, HDFS, etc.
For more details, please read my blog post:
- http://woonsanko.blogspot.com/2016/08/cant-we-store-huge-amount-of-binary.html

## Adding dependencies to use Jackrabbit ```VFSDataStore```

Please see [highlighted dependencies in cms/pom.xml](cms/pom.xml#L17-L45). Basically you need to include ```jackrabbit-vfs-ext``` jar dependency and other VFS2 backend dependencies optionally in your project.

## VFS Backend Test Options: SFTP or WebDAV.

Enable either WebDAV server (Option 1) or SFTP server (option 2) as explained below.

## Option 1: Using an SFTP server

In [pom.xml](pom.xml), it is configured to use a SFTP backend by default:

```xml
              <repo.config>file://${project.basedir}/conf/repository-vfs2-sftp.xml</repo.config>
              <!-- <repo.config>file://${project.basedir}/conf/repository-vfs2-webdav.xml</repo.config> -->
              <!-- <repo.config>file://${project.basedir}/conf/repository-db.xml</repo.config> -->
```

Open [conf/repository-vfs2-sftp.xml](conf/repository-vfs2-sftp.xml) and edit the connection information properly.

## Option 2: Install and Run an example WebDAV server

Open [pom.xml](pom.xml), and comment out the first ```<repo.config>``` element and uncomment the second one instead:

```xml
              <!-- <repo.config>file://${project.basedir}/conf/repository-vfs2-sftp.xml</repo.config> -->
              <repo.config>file://${project.basedir}/conf/repository-vfs2-webdav.xml</repo.config>
              <!-- <repo.config>file://${project.basedir}/conf/repository-db.xml</repo.config> -->
```

In this demo project, I used [WsgiDAV](https://github.com/mar10/wsgidav) server as WebDAV backend server.

You can install/configure it following this page:
- [http://wsgidav.readthedocs.io/en/latest/installation.html](http://wsgidav.readthedocs.io/en/latest/installation.html)
- [http://wsgidav.readthedocs.io/en/latest/user_guide_configure.html](http://wsgidav.readthedocs.io/en/latest/user_guide_configure.html)

If you installed it, you could move to ```wsgidav``` subfolder and run the following to make this demo run:

        cd wsgidav
        ./start-wsgidav.sh

The above command will start WebDAV server at port 8888 [http://localhost:8888](http://localhost:8888)
with the root directory at ```wsgidav/davshare```.

Open [conf/repository-vfs2-webdav.xml](conf/repository-vfs2-webdav.xml) and edit the connection information if necessary.

## Option 3: Using the Database DataStore (the default option in Hippo) instead of VFS DataStore

Open [pom.xml](pom.xml), and comment out the first ```<repo.config>``` element and uncomment the second one instead:

```xml
              <!-- <repo.config>file://${project.basedir}/conf/repository-vfs2-webdav.xml</repo.config> -->
              <!-- <repo.config>file://${project.basedir}/conf/repository-vfs2-sftp.xml</repo.config> -->
              <repo.config>file://${project.basedir}/conf/repository-db.xml</repo.config>
```

## Run the Demo Project

This project uses the Maven Cargo plugin to run CMS ("Content Authoring") and SITE ("Content Delivery") web applications locally in Tomcat.
From the project root folder, execute:

        mvn clean verify && mvn -P cargo.run

The default Jackrabbit repository directory is located at ```target/storage```.

After your project is set up, access the CMS at http://localhost:8080/cms/ and the site at http://localhost:8080/site/.
Logs are located in target/tomcat8x/logs

If you take a look at the terminal of the WsgiDAV server, then you can already see many logs about content uploading. This is because Hippo Repository bootstraps some example binary content to the repository storage during the initialization.

## Repository Configuration

Repository configuration is located at the following:

- VFS2/WebDAV : [repository-vfs2-webdav.xml](conf/repository-vfs2-webdav.xml), which customizes the ```DataStore``` using ```VFSDataStore``` like the following:

```xml
          <DataStore class="org.apache.jackrabbit.vfs.ext.ds.VFSDataStore">
            <param name="config" value="${catalina.base}/conf/vfs2-datastore-webdav.properties" />
            <!-- VFSDataStore specific parameters -->
            <param name="asyncWritePoolSize" value="10" />
            <!--
              CachingDataStore specific parameters:
                - secret : key to generate a secure reference to a binary.
            -->
            <param name="secret" value="123456"/>
            <!--
              Other important CachingDataStore parameters with default values, just for information:
                - path : local cache directory path. ${rep.home}/repository/datastore by default.
                - cacheSize : The number of bytes in the cache. 64GB by default.
                - minRecordLength : The minimum size of an object that should be stored in this data store. 16KB by default.
                - recLengthCacheSize : In-memory cache size to hold DataRecord#getLength() against DataIdentifier. One item for 140 bytes approximately.
            -->
            <param name="minRecordLength" value="1024"/>
            <param name="recLengthCacheSize" value="10000" />
          </DataStore>
```

You can also define VFS specific properties (e.g., ```${catalina.base}/conf/vfs2-datastore-webdav.properties```) like the following:

```
        baseFolderUri = webdav://tester:secret@localhost:8888/vfsds
        
        # Properties to build org.apache.commons.vfs2.FileSystemOptions at runtime when resolving the base folder.
        # Any properties, name of which is starting with 'fso.', are used to build FileSystemOptions
        # after removing the 'fso.' prefix. See VFS2 documentation for the detail.
        fso.http.maxTotalConnections = 200
        fso.http.maxConnectionsPerHost = 200
        fso.http.preemptiveAuth = false
```

- VFS2/SFTP : [repository-vfs2-sftp.xml](conf/repository-vfs2-sftp.xml), which customizes the ```DataStore``` using ```VFSDataStore``` like the following:

```xml
          <DataStore class="org.apache.jackrabbit.vfs.ext.ds.VFSDataStore">
            <param name="config" value="${catalina.base}/conf/vfs2-datastore-sftp.properties" />
            <!-- VFSDataStore specific parameters -->
            <param name="asyncWritePoolSize" value="10" />
            <!--
              CachingDataStore specific parameters:
                - secret : key to generate a secure reference to a binary.
            -->
            <param name="secret" value="123456"/>
            <!--
              Other important CachingDataStore parameters with default values, just for information:
                - path : local cache directory path. ${rep.home}/repository/datastore by default.
                - cacheSize : The number of bytes in the cache. 64GB by default.
                - minRecordLength : The minimum size of an object that should be stored in this data store. 16KB by default.
                - recLengthCacheSize : In-memory cache size to hold DataRecord#getLength() against DataIdentifier. One item for 140 bytes approximately.
            -->
            <param name="minRecordLength" value="1024"/>
            <param name="recLengthCacheSize" value="10000" />
          </DataStore>
```

You can also define VFS specific properties (e.g., ```${catalina.base}/conf/vfs2-datastore-sftp.properties```) like the following:

```
        baseFolderUri = sftp://tester:secret@localhost/vfsds
        
        # Properties to build org.apache.commons.vfs2.FileSystemOptions at runtime when resolving the base folder.
        # Any properties, name of which is starting with 'fso.', are used to build FileSystemOptions
        # after removing the 'fso.' prefix. See VFS2 documentation for the detail.

        # when the identity file (your private key file) is used instead of password
        #fso.sftp.identities = /home/hippo/.ssh/id_rsa
```


## Test Scenarios

### Visit page retrieving binary images

- Visit http://localhost:8080/site/news.
- Click on a new article link.
- The photo inside the article was retrieved from the backend (WebDAV or SFTP) server through ```VFSDataStore``` component.
- You can see some logs in the WsgiDAV server if it's used.

### Visit CMS ("Content Authoring") to upload binary images / assets (e.g, pdfs)

- Visit http://localhost:8080/cms/
- Log on by admin/admin
- Click on the "Content" perspective.
- Select "Image" or "Asset" in the dropdown located near the left top corner.
- Click or create a folder and try to upload image files or asset (e.g, PDF) files.
- Tip: you need to hover your mouse on a tree node to find and click on the action buttons.
- You can see some logs in the WsgiDAV server as well if it's used.

### (Optional) Discovering Content Identity through CMS Console

- Visit http://localhost:8080/cms/console/
- Log on by admin/admin
- Find any binary property (under gallery or asset) which contains data bigger than 16KB (by default, a ```CachingDataStore``` stores binary only when the data is bigger than 16KB).
- You will be able to find "Content Identity: (Show)" next to the "Upload" button. Click on "(Show)" link to retrieve the content identity.
- You can possibly figure out where the real binary file is stored in the backend (WebDAV or SFTP) by the "Content Identity" value.
