; Assembled using GAS for z80-unknown-coff.
; Tested using z80sim CP/M 2.2 emulator.

	ORG	100H

section .text

	global	_start
_start:
	LD	DE, message
	LD	C, 9
	CALL	5
	CALL	0

section .data

message:
	DB	"hello, world", 0x0d, 0x0a, 0x24
