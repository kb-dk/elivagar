# Transfer
Elivagar will transfer the content files and the metadata to ingest/update areas, when they all have been collected or updated for a given e-book og audio-book.

There are two types of transfer: ingest and update.
These have different destinations for the transfer of the files (ingest and update are handled different by the Kuana receiver).

When an ingest or update has been performed, it will be registered in the registry - see section below.

It is configurable what is required for the ingest (the initial transfer), but generally it will require the following files:
* The given content file (pdf, epub, mp3, etc.)
* The Fits characterization of the content file
* The metadata record from Publizon.
* The MODS record from the library system (Alma)

For an update, one of the files must be newer than the latest update date or the ingest date (if it has not been updated before),
or the content file must have a different checksum and a newer date than its latest ingest/update.
In addition, an update can also occur if a book is received in a new file format.

# Registry
The registry file keeps track of if and when a e-book/audio-book has been ingested and updated, and also which checksum and last-modified date the book had.
It will be created during the ingest (the initial transfer), and it will be updated whenever an update-transfer it performed.

It will have one ingest date for when all data and metadata has been initially transferred. 

There must be written a MD5 checksum for each content-file and the last-modified date for the file.
Whenever a content file is updated, then it must have a new entry for both the checksum and last-modify date.

And then there will be a update date for each time updates have been transferred.

It should look like the following:

```
ingest date: 1550379466473
MD5: 2a12f9fb-c133-4b47-bf80-90886518924b.pdf##9484d1aae1585171b22e563b0cfbf2d1
File date: 2a12f9fb-c133-4b47-bf80-90886518924b.pdf##1570625978000
update date: 1585221562397
```
