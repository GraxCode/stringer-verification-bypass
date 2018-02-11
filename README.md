###About
This tool is made for patching jar archives obfuscated and signed with stringer 3.0.x
###What does it do?
Stringer encrypts Strings and decrypts them only if nothing in the jar has been modified.
It usually has one or more classes used for decryption and one method that returns a long value. By pressing "Patch" that value is calculated and injected into that method.
"Patch Manifest" removes the signature and certificates.

![Screenshot 1](https://i.imgur.com/Ckr7CFm.png)

Tested with Stringer 3.0.3 and 3.0.9