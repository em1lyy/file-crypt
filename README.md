# file-crypt
A program to encrypt files, so they can be deleted without a possibility to restore the files.

It is completely written in Java, GUI designed in WindowBuilder for Eclipse, IDE used is Eclipse Oxygen.2

How it works:
It encrypts a specific File with RSA Cipher and deletes the Encryption key afterwards, so restoring the File
from the NTFS-Index is completely useless.

External Libaries used:
(No external Libaries used)

Things to know:
Since 1.8 a specific RSA-Encrypion File Format is used.
It is called "FCRSA" (Stands for "FileCrypt-RSA") and is made by me, Jonas Jaguar.
With FCRSA, you can Save information about the Encryption process direclty to the File.
You can also decide if the Private Key should be saved to the File or not.
Keys and Failed Lines are Base64-Encrypted (Since FCRSA 1.1, FileCrypt 1.8 still uses FCRSA 1.0.
                                            FileCrypt 1.9 will use FCRSA 1.1)
for extra Security.

DOWNLOAD LINK FOR INSTALLER (Version 1.7.6):

https://mega.nz/#!VzZmQKCD!C-OM-30jpvIc_UXa6x_FMuMlkUs8HIni2-WVSh-j49M
