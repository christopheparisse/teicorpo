rsync -auv --exclude="*/.*"  --exclude="builds/" --exclude="*.class" --exclude="*.git" --exclude="*node_modules/*" /projets/workspace/ortolang/src .
rsync -auv --exclude="*/.*"  --exclude="builds/" --exclude="*.class" --exclude="*.git" --exclude="*node_modules/*" /projets/workspace/ortolang/test .
