eon.pdf : eon.tex copyright.tex raw.tex *.pdf
	pdflatex eon
	pdflatex eon

raw.tex : raw.sh
	./raw.sh

%.pdf : %.eps
	ps2pdf -dEPSCrop $<

clean :
	rm -f *.aux eon.pdf
