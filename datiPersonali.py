from smartcard.System import readers
import array

r = readers()
reader = r[0]
print "Lettura dati personali da ", reader
connection = reader.createConnection()
connection.connect()

CAMPI = ["emettitore","data_rilascio","data_scadenza","cognome","nome","data_nascita","sesso","cod_fiscale","comune_emissione","comune","via"]


#SELEZIONE DIRETTA DI EF_Dati_personali
SELECT_DATI = [0x00, 0xA4, 0x08, 0x00, 0x04, 0x11, 0x00, 0x11, 0x02]
data, sw1, sw2 = connection.transmit(SELECT_DATI)

#leggiamo i dati
#CLS 00, istruzione B0 (leggi i dati binari contenuti nel file)
READ_BIN = [0x00, 0xB0, 0x00, 0x00, 0x00, 0x00, 0xff]
data, sw1, sw2 = connection.transmit(READ_BIN)

#data contiene i dati anagrafici in formato binario
#trasformiamo il tutto in una stringa
stringa_dati_personali = array.array('B', data).tostring()

dati = {}

i = 0
start = 6
while (i < len(CAMPI)):

	fldSize = int(stringa_dati_personali[start:start+2], 16)
	start += 2

	if(fldSize == 0):
		continue

	dati[CAMPI[i]] = stringa_dati_personali[start:start+fldSize]
	start += fldSize

	i += 1

print "Salvati dati di " + dati['nome'].title()+' '+dati['cognome'].title()

#salva i dati su un file:
f  = open(dati['nome']+'_'+dati['cognome'],"w+")
for k in dati.keys():
	f.write(k+': '+dati[k]+"\n")
f.close()
