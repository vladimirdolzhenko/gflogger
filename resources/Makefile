RSVG=rsvg-convert

SOURCES=ring1.svg logo.svg

OBJECTS=ring1.png logo.png

.PHONY: all
all: $(OBJECTS)

.PHONY: clean
clean:
	$(RM) $(OBJECTS)

tags: TAGS

.PHONY: all
all: 
	
.SUFFIXES:
.SUFFIXES: .svg .png

.svg.png: styles.css
	$(RSVG) -f png $< -o $@

TAGS: $(OBJECTS)
