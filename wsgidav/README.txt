[My steps to install WsgiDAV on a Macbook (v10.x)]

1. Install PIP

    $ cd /tmp
    $ curl https://bootstrap.pypa.io/get-pip.py -o get-pip.py
    $ python get-pip.py

2. Install WsgiDAV

    $ pip install --upgrade wsgidav

3. Install Cheroot

    $ pip install cheroot --ignore-installed six

Now it's ready to run `start-wsgidav.sh`.
