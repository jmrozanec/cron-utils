*This branch only contains a settings.xml and a settings-security.xml file, so that we enable deployments for each successful build on master branch.*

## Implementing continuous delivery for cron-utils
Cron utils is a library for parsing, validations and human readable descriptions as well as date/time interoperability.
In this section we explain how we configures travis-ci to reach a continuous delivery pipeline.


### References
Check the following links for information on building a pipeline using Travic-CI:
* https://coderwall.com/p/9b_lfq/deploying-maven-artifacts-from-travis
* http://www.theguardian.com/info/developer-blog/2014/sep/16/shipping-from-github-to-maven-central-and-s3-using-travis-ci
* https://vzurczak.wordpress.com/2014/09/23/deploying-to-maven-repositories-from-tavis-ci/


### Procedure description
* Create a clean branch called travis, as specified in the first post.
* Add a settings.xml file referencing environment variables.
* Add a settings-security.xml file with a master password you generated. Instructions can be found [here](https://maven.apache.org/guides/mini/guide-encryption.html).
* Encrypt your repo password as specified at [this post](https://maven.apache.org/guides/mini/guide-encryption.html), and register it at Travis CI environment variables.
* Configure .travis.yml on all branches whitelisting just the branches to be built (master in our case).
* Add the following lines to your .travis.yml:

        before_install: "git clone -b travis `git config --get remote.origin.url` target/travis"
        script: "mvn deploy --settings target/travis/settings.xml -Dsettings.security=target/travis/settings-security.xml"

* Push changes and enjoy!
