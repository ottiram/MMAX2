# MMAX2
Official repo of the MMAX2 annotation tool

### NEW Release 1.14.005 (March 21 2021):

Download current version (.jar only) [here](jar_archive/1.14.005/MMAX2.jar), or clone the repo to also download docs and sample data.

Previous versions are available in the MMAX2 [jar_archive](jar_archive/)

#### Changes:
##### Upon saving markable files, a time-stamped backup file will be created.
##### Command-line parameter -no_validation can be passed to skip validation on start-up.
##### The default line spacing has been increased.
##### Attribute panel setting 'Warn on extra attributes' is set to 'False' by default.
##### When loading a .mmax file, the current file (if any) is pre-selected in the file chooser dialog.
##### Reduced verbosity of console output.
##### Some bug fixes:
###### Issue 2 (pointer-relation rendering of discontiuous markables)
###### Customized line spacing is retained after style-sheet reapplication


#####

NEW (16.11.2020): Added mmax2_flex.sh, which accepts as an optional second parameter an alternative common_paths file.

NEW (16.02.2020): MMAX2 has been updated to work with current Java versions. The previous version was based on outdated Xalan and Xerces versions, which have been replaced in this repo.

