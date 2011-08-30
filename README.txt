PPI Survey Migration to Questionnaire model of Mifos 2.0

NOTE: Run only after Upgrading to Mifos 2.0+

Stop Mifos server and backup Mifos database before running migration script.

Use run.sh OR run.bat according to operating system, BUT if custom mifos 
configuration location (MIFOS_CONF) is being used for Mifos then correctly 
set MIFOS_CONF environment variable in run script.

http://mifosforge.jira.com/wiki/display/MIFOS/Mifos+Configuration+Locations

An error log file will be created in the script directory (mifos-ppimigration.log).

Check error logs for failures after migration process

Start Mifos and verify some PPI responses for the previous client.