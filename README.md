# Custom-SCIM-Generator

1. Provide the correct details to connect to the LDAP and the search base in the config.properties file before executing.
2. The UPDATE_ENTRY can be set to `true` or `false`.
    If set to `false` - It will list all the users who does not have a SCIM_ID. When running the script for the first time, run the script 
    by setting UPDATE_ENTRY to false. Then you can get a list of all the users who will be updated through this sctipt.
    If set to `true` - It will update the users who does not have a SCIM_ID with a new SCIM_ID.
3. To run the script first compile the CustomSCIMGenerator.java class by running `javac CustomSCIMGenerator.java`
4. Then run the file by running `java CustomSCIMGenerator`.
