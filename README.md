# sorting documentary photos
simple java app to sort duplicate photos

## Requirements
- **JDK Version 17.0** or latest
-  `.jpg`, `.jpeg`, and `.png` formats.

## Folder Setup
make sure your directory structure looks like this:
```text
├── ImageSorter.java
├── source/           <-- Put your photos here
├── fix/              <-- fix photos will be moved here
└── duplicate/        <-- duplicate photos will be moved here
```

## Run
place all images you want to sort inside the `source/` folder.
and run 
```bash
java ImageSorter.java
```
Use the Fix button to move the photo to `./fix/` or the Duplicate button to move it to `./duplicate/.`
