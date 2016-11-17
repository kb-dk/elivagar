
# elivagar
Fetch and marshall e-book metadata from the PubHub web service (https://service.pubhub.dk/).


Prerequisite
--------------------------
This requires [pubhubWSClient](https://github.com/Det-Kongelige-Bibliotek/pubhubWSClient).
* git clone https://github.com/Det-Kongelige-Bibliotek/pubhubWSClient.git
* cd pubhubWSClient
* mvn clean install


Making the elivagar package without running the unittests
----------------------------------------------------------

mvn clean -Dmaven.test.skip=true package


Producing a new elivagar package for deployment
----------------------------------------------------------

Update the version variable in pom.xml

Produce the new package using Maven


Running the unit-test against pubhub
----------------------------------------------------------
The tests running against pubhub requires a file with the license key guid.
This file must be placed in the home folder of the linux-user, and it must be named 'pubhub-license.txt'.
