# Graph INference Model (GIN)

Implementation of Bevan Koopman's retrieval model descriped in:

B Koopman, G Zuccon, P Bruza, L Sitbon, M Lawley. *Information retrieval as semantic inference: A graph inference model applied to medical search*. Information Retrieval, 19(1):6–37, 2015

## Setup and configuration

### Convert SQL to DOT

```{bash}
sqlite3 snomed_rel.db 'select cui1, cui2, reltype from crel wrelcharacteristic=3);' | sed -E "s/(^[0-9]+)\|([0-9]+)\|([0-9]+)/\1 -> \2 \[label=\"\3\"\];/g"
```

### Enable indexing numeric characters

Currently Terrier ignores terms that contain a sequence of numeric characters. This feature should be disable for concept-based indexes. It can be disabled by comments out the following code in `org.terrier.indexing.tokenisation.EnglishTokeniser:check(String s)`:

```{java}
if (counter > maxNumOfSameConseqLettersPerTerm || counterdigit > maxNumOfDigitsPerTerm)
	return "";
```

## Running

```{bash}
./bin/gin_retrieval.sh
```