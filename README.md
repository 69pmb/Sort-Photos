[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=pmb:sort-photos&metric=alert_status)](https://sonarcloud.io/dashboard?id=pmb:sort-photos)

# Sort-Photos

This project is a Java program to sort photos by _Year_ and then by _Month_ and to rename files with its taken date time.  
The exe file can be downloaded [here](https://github.com/69pmb/Sort-Photos/releases/download/0.2.0/sort-photos-0.2.0.exe).  
It is required to have [Java installed](https://java.com), with the minimum version of 11.

## Goals

The purpose of this application is to automatically rename your photos with the date they were taken.

If a photo was taken on March 24, 2020 at 11:54 am, it will be renamed for instance `2020-03-24 11h54m14.jpg`.  
The output format of the file can be set.

The application also allows to move the photos in folders by year then by month.  
So by processing for example a folder containing several hundred photos from 2019 and 2020, the files will be moved to a `2019` and `2020` folder then to a `01.2019`, `02.2019` folder, etc.

The application also manages duplicates by detecting identical or similar photos.

## Usage

### The general interface

<p align="center"><img src=".\documentation\Sort1.PNG" height="500"/></p>

1. To open the log file (`error.log`) with the default file editor, useful in case of error
2. To open this online help
3. Exit the app
4. Dropdown to switch language from _English_ to _French_
5. Selected directory to process
6. Browse files to select another directory
7. Save button to change the default directory
8. Property panel, see the [properties section](#the-properties) for more details
9. The application detect if the selected directory is a _Annual folder_ (e.g. `2020`), a _Monthly folder_ (e.g. `05.2020`) or a regular folder.  
If it's a regular one, photos will be moved to annual and the monthly folders, if it's a monthly they will not be moved and if it's annual they will be placed to monthly folders
10. Button to launch the process

### The properties

<p align="center"><img src=".\documentation\Sort2.PNG" height="500"/></p>

11. The output file format, based on the [java date format](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/text/SimpleDateFormat.html)
12. Photo extensions to process, comma separated
13. Video extensions to process, comma separated
14. If the photos must be moved or not
15. If the photos detected as identical must be overridden. Identical pictures have the same taken date, same memory size, same camera model and the same file extension.
16. If similar photos (i.e. same output file name) must be automatically suffixed by `~1`, `~2`, etc. If not they will be handled in the [duplicate dialog](#duplicate-dialog)
17. Ignore already formatted photos
18. Ignore photos with no taken date
19. Choose what to do with photos with no taken date: take their creation date, modification date or their filename.  
Parsing their name will also use the [java date format](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/text/SimpleDateFormat.html).  
A dialog will be shown to confirm the choice, for more read the [No taken date dialog](#no-taken-date-dialog) part.
20. Reset the properties with the last saved configuration
21. Save the properties in the configuration file

### No taken date dialog

<p align="center"><img src=".\documentation\Sort3.PNG" height="500"/></p>

22. This confirmation dialog is shown when a photo has no taken date
23. This button gives the user the ability to stop all the process (see the [finish screen when canceled](#finish-screen-when-canceled))
24. When checked the dialog will not be shown again if another picture has no taken date.  
The choice (between _Yes_ or _No_) will be kept for all following photos
25. The file will be renamed using the chosen option in the properties
26. The file will not be renamed
27. Button to stop the process, accessible only if no dialog are shown (see the [finish screen when canceled](#finish-screen-when-canceled))
28. Progression: percent of pictures processed

### Duplicate dialog

<p align="center"><img src=".\documentation\Sort4.PNG" height="500"/></p>

29. This dialog opens when a picture is set to be renamed but another picture has this name too.  
The dialog is here to choose between various options to handle this case  
The count of duplicate pictures is shown in the top left corner
30. The picture to rename
31. The existing picture with the taken name, it can have been renamed just before during the process 
32. Rotate button to help compare, in case for instance that 2 pictures are the same but one have been rotated before
33. Pictures metadata: name, creation, modification & taken dates, device name and dimension
34. The picture is skipped and is not renamed
35. The picture to rename replaces the existing picture
36. The picture to rename is renamed and suffixed: `2021-02-14-13h14m52~1.jpg`.  
If another picture has already been suffixed, another duplicate dialog will open to compare the 1st and 2nd suffixed pictures.  
If the _suffix_ option is again chosen it will increment the suffix, e.g. `~2`
37. The picture to rename is deleted
38. The process will stop (see the [finish screen when canceled](#finish-screen-when-canceled))

### Failed to parse date for taken date dialog

<p align="center"><img src=".\documentation\Sort5.PNG" height="500"/></p>

39. This dialog is shown when the parsing of a filename (i.e. the fallback property is set to _parsing_) failed when trying to recover a taken date
40. The process will stop (see the [finish screen when canceled](#finish-screen-when-canceled))
41. Confirmation button

### Finish screen with errors

<p align="center"><img src=".\documentation\Sort6.PNG" height="500"/></p>

42. Message when the process is finished with errors.  
The log file `error.log` is accessible next to the executable.  
If a log file existed it will be archived with its creation date

### Finish screen when canceled

<p align="center"><img src=".\documentation\Sort7.PNG" height="500"/></p>

43. Message when the process is canceled (with the stop process buttons)

### Finish screen

<p align="center"><img src=".\documentation\Sort8.PNG" height="500"/></p>

44. Message when the process is finished with no error
