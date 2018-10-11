# hippo-davstore-demo

Hippo CMS Demo using Jackrabbit ```VFSDataStore``` against either **SFTP server** or **WebDAV server** as a binary content storage.


#### Warning: Demo with WebDAV not working at the moment!

    Please test it with an SFTP server for now as the demo with WebDAV is broken.
    Apache Commons VFS 2 still depends on the old Jackrabbit WebDAV library and Commons HTTP Client 3.x
    for the **webdav** protocol.
    However, Apache Jackrabbit WebDAV library has upgraded HttpClient to v4.x since 2.14 (ref: JCR-4065).
    So, those two no more work with each other.
    It is something to fix in Apache Commons VFS project.

Jackrabbit ```VFSDataStore``` (available since Jackrabbit 2.13.2) is using Commons VFS 2 to get access to various backend storages such as local file system, SFTP, WebDAV, etc.
For more details, please read my blog post:
- http://woonsanko.blogspot.com/2016/08/cant-we-store-huge-amount-of-binary.html

## Adding dependencies to use Jackrabbit ```VFSDataStore```

Please see [highlighted dependencies in cms/pom.xml](cms/pom.xml#L17-L45). Basically you need to include ```jackrabbit-vfs-ext``` jar dependency and other VFS2 backend dependencies optionally in your project.

## VFS Backend Test Options: SFTP or WebDAV.

Enable either SFTP server (option 1) or WebDAV server (Option 2) as explained below.

### Option 1: Using an SFTP server

In [pom.xml](pom.xml), it is configured to use a SFTP backend by default:

```xml
              <repo.config>file://${project.basedir}/conf/repository-vfs2-sftp.xml</repo.config>
              <!-- <repo.config>file://${project.basedir}/conf/repository-vfs2-webdav.xml</repo.config> -->
              <!-- <repo.config>file://${project.basedir}/conf/repository-db.xml</repo.config> -->
              <!-- <repo.config>file://${project.basedir}/conf/repository-aws-s3.xml</repo.config> -->
```

**NOTE**: In addition, ```VFSFileSystem``` can also be configured for **Repository**, **Workspace** and **Versioning**
even if it's configured only for **Versioning** in [conf/repository-vfs2-sftp.xml](conf/repository-vfs2-sftp.xml) at the moment.
This is to demonstrate the new feature, [JCR-4354: VFS File System](https://issues.apache.org/jira/browse/JCR-4354).
By the example configuration, all the version data will be stored in the SFTP backend.

Open [conf/repository-vfs2-sftp.xml](conf/repository-vfs2-sftp.xml) and edit the connection information properly.

### Option 2: Install and Run an example WebDAV server

If you want to use a WebDAV server as backend, open [pom.xml](pom.xml), uncomment the second ```<repo.config>``` element
and comment out the others:

```xml
              <!-- <repo.config>file://${project.basedir}/conf/repository-vfs2-sftp.xml</repo.config> -->
              <repo.config>file://${project.basedir}/conf/repository-vfs2-webdav.xml</repo.config>
              <!-- <repo.config>file://${project.basedir}/conf/repository-db.xml</repo.config> -->
              <!-- <repo.config>file://${project.basedir}/conf/repository-aws-s3.xml</repo.config> -->
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

### Option 3: Using the Database DataStore (the default option in Hippo) instead of VFS DataStore

If you want to use the default Database DataStore, open [pom.xml](pom.xml), uncomment the third ```<repo.config>``` element
and comment out the others:

```xml
              <!-- <repo.config>file://${project.basedir}/conf/repository-vfs2-webdav.xml</repo.config> -->
              <!-- <repo.config>file://${project.basedir}/conf/repository-vfs2-sftp.xml</repo.config> -->
              <repo.config>file://${project.basedir}/conf/repository-db.xml</repo.config>
              <!-- <repo.config>file://${project.basedir}/conf/repository-aws-s3.xml</repo.config> -->
```

### Option 4: Using the AWS S3 DataStore instead of VFS DataStore

If you want to use the AWS S3 Database DataStore, open [pom.xml](pom.xml), uncomment the third ```<repo.config>``` element
and comment out the others:

```xml
              <!-- <repo.config>file://${project.basedir}/conf/repository-vfs2-sftp.xml</repo.config> -->
              <!-- <repo.config>file://${project.basedir}/conf/repository-vfs2-webdav.xml</repo.config> -->
              <!-- <repo.config>file://${project.basedir}/conf/repository-db.xml</repo.config> -->
              <repo.config>file://${project.basedir}/conf/repository-aws-s3.xml</repo.config>
```

## Run the Demo Project

This project uses the Maven Cargo plugin to run both CMS (http://localhost:8080/cms/ for "Content Authoring") web application
and SITE (http://localhost:8080/site/ for "Content Delivery") web application locally in Tomcat.

From the project root folder, execute:

        mvn clean verify && mvn -P cargo.run -Drepo.path=storage

After your project is set up, access the CMS at http://localhost:8080/cms/ and the site at http://localhost:8080/site/.
Logs are located in target/tomcat8x/logs

If you use a WebDAV server as backend, you can take a look at the terminal of the WsgiDAV server.
You will see many logs about content uploading. This is because Hippo Repository bootstraps some example binary content to the repository storage during the initialization.

## Repository Configuration

Repository configuration is located at the following:

- VFS2 / SFTP : [conf/repository-vfs2-sftp.xml](conf/repository-vfs2-sftp.xml), which sets ```DataStore``` using ```VFSDataStore``` component.

  More VFS backend specific detail for backend SFTP server is configured in [conf/vfs2-datastore-sftp.properties](conf/vfs2-datastore-sftp.properties).

  Also, this SFTP configuration demonstrates the separate feature [JCR-4354: VFS based FileSystem](https://issues.apache.org/jira/browse/JCR-4354), by using ```VFSFileSystem``` for version storage by default. See [JCR-4354: VFS based FileSystem](#new-feature-jcr-4354-vfs-file-system-for-version-storage-for-example) for detail.

- VFS2 / WebDAV : [conf/repository-vfs2-webdav.xml](conf/repository-vfs2-webdav.xml), which sets ```DataStore``` using ```VFSDataStore``` component.

- S3 : [conf/repository-aws-s3.xml](conf/repository-aws-s3.xml), which sets ```DataStore``` using ```S3DataStore``` component.

  More VFS backend specific detail for backend WebDAV connection is shown in [conf/vfs2-datastore-webdav.properties](conf/vfs2-datastore-webdav.properties).

## Test Scenarios

### Visit page retrieving binary images

- Visit http://localhost:8080/site/news.
- Click on a new article link.
- The photo inside the article was retrieved from the backend (SFTP or WebDAV) server through the ```VFSDataStore``` component.

### Visit CMS ("Content Authoring") to upload binary images / assets (e.g, pdfs)

- Visit http://localhost:8080/cms/
- Log on by admin/admin.
- Click on the "Content" perspective.
- Select "Image" or "Asset" in the dropdown located near the left top corner.
- Click or create a folder and try to upload image files or asset (e.g, PDF) files.
- Tip: you need to hover your mouse on a tree node to find and click on the action buttons.

### (Optional) Discovering Content Identity through CMS Console

- Visit http://localhost:8080/cms/console/
- Log on by admin/admin.
- Find any binary property (under gallery or asset) which contains data bigger than 16KB (by default, a ```CachingDataStore``` stores binary only when the data is bigger than 16KB).
- You will be able to find "Content Identity: (Show)" next to the "Upload" button. Click on "(Show)" link to retrieve the content identity.
- You can possibly figure out where the real binary file is stored in the backend (SFTP or WebDAV) by the "Content Identity" value.

### (New Feature) JCR-4354: VFS File System (for version storage for example)

- Reference: [https://issues.apache.org/jira/browse/JCR-4354](https://issues.apache.org/jira/browse/JCR-4354).
- Visit http://localhost:8080/cms/
- Log on by admin/admin.
- Open a news document through the Content Perspective / Documents.
- Edit the news document to add some text in the title for example, and save & close it.
- Publish the document and you can see revision history through Document / Revision History menu.
- You may select an old revision, which open a new tab to show the old revision. Under the hood, it reads the versioned
content from the backend SFTP server.

### (New Feature) Investigating Tool for bundle files in VFS directory

The [bundlereader](bundlereader) submodule provides a tool to investigate bundle files stored in VFS FileSystem.

After building, you can find the executable uber jar file under its build output directory. e.g, `bundlereader/target/hippo-davstore-demo-bundlereader-*.jar`.

Now suppose you have access a bundle file in your local file system. Then you can run the tool to investigate the content of the file
like the following:

```bash
# With a file path:
$ java -jar target/hippo-davstore-demo-bundlereader-0.2.0-SNAPSHOT.jar /Users/tester/vfsfs/items/21/2a/4141c7064ac68b2768d6281989c9.n

# Or simply with a UUID after the base path (e.g, /Users/tester/vfsfs/items/):
$ java -jar target/hippo-davstore-demo-bundlereader-0.2.0-SNAPSHOT.jar /Users/tester/vfsfs/items/212a4141-c706-4ac6-8b27-68d6281989c9
```

Example output:

```
$ java -jar target/hippo-davstore-demo-bundlereader-0.2.0-SNAPSHOT.jar /Users/tester/vfsfs/items/212a4141-c706-4ac6-8b27-68d6281989c9
ID: 212a4141-c706-4ac6-8b27-68d6281989c9
Parent ID: 9e440e6d-e93a-4d5f-913b-9c3b0867c812
Node type: {http://www.onehippo.org/jcr/hippogallery/nt/2.0}imageset
Mixin types: []
Referenceable? false
Shared set: []
Properties:
  - {http://www.onehippo.org/jcr/hippo/nt/2.0.4}paths (String multiple): [212a4141-c706-4ac6-8b27-68d6281989c9, 9e440e6d-e93a-4d5f-913b-9c3b0867c812, 889e1e98-6bc5-4320-8d0b-31c8fedd9c5c, 6a3a88f5-6e99-4323-ad9a-ca661ca50514, 37aa21b2-d5dd-4ac2-9e1c-9cb0f82dd099, ed307dfc-5774-4ac7-9feb-79b57e058bb2, cafebabe-cafe-babe-cafe-babecafebabe]
  - {http://www.onehippo.org/jcr/hippo/nt/2.0.4}availability (String multiple): [live, preview]
  - {http://www.onehippo.org/jcr/hippogallery/nt/2.0}filename (String): [banner-1.jpg]
Children:
  - {http://www.onehippo.org/jcr/hippogallery/nt/2.0}thumbnail (b962a623-bf1f-4f41-b629-1dccd24e4bbe)
  - {http://www.onehippo.org/jcr/hippogallery/nt/2.0}original (281c722c-53c7-4c67-8292-2c0e65d7dd48)
```

## Note: Jackrabbit namespace registry files in the repository directory

If you run CMS with `VFSFileSystem` for both the **Workspace** and **Versioning**, but not for **Repository**,
you should be cautious when cleaning up the repository directory (`storage/` folder for example as specified by `-Drepo.path=storage`)
because the default `FileSystem` of **Repository** is that of `LocalFileSystem`.

- In that case, if you clean up the existing the repository directory (e.g, `storage/`),
  then Jackrabbit could fail to restart due to namespace exceptions. So you must keep and restore the following files at least before restart:

        - repository/namespaces/ns_idx.properties
        - repository/namespaces/ns_reg.properties 

- As an example, before cleaning up the the repository directory (`storage/` in this example), backup and keep the files (`storage/repository/namespaces/*.properties)`
  and clean up the other files and folders.
- Now you can restart with a cleaned up repository directory without any problem:

        mvn clean verify && mvn -P cargo.run -Drepo.path=storage

